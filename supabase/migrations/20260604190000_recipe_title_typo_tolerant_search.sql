-- search_recipes only matched the title with ILIKE, so a query with a typo
-- found nothing. word_similarity(p_query, title) from pg_trgm checks how well
-- the query matches any word inside the title instead of comparing the whole string.
-- The threshold 0.45 is lower than the default so a transposed letter or two still matches.
CREATE OR REPLACE FUNCTION public.search_recipes(
    p_query            TEXT    DEFAULT NULL,
    p_difficulties     TEXT[]  DEFAULT ARRAY[]::text[],
    p_time_windows     TEXT[]  DEFAULT ARRAY[]::text[],
    p_meal_type        TEXT    DEFAULT NULL,
    p_categories       TEXT[]  DEFAULT ARRAY[]::text[],  -- category names (categories.name), not the category UUID
    p_ingredient_roots UUID[]  DEFAULT ARRAY[]::uuid[],
    p_pantry_roots     UUID[]  DEFAULT ARRAY[]::uuid[],
    p_expiring_roots   UUID[]  DEFAULT ARRAY[]::uuid[],
    p_min_available    INT     DEFAULT 0,
    p_min_expiring     INT     DEFAULT 0,
    p_order_by         TEXT    DEFAULT 'title',
    p_limit            INT     DEFAULT 60
)
RETURNS TABLE (
    id                   UUID,
    title                TEXT,
    image_url            TEXT,
    preparation_time     INT,
    difficulty           TEXT,
    available_count      INT,
    required_count       INT,
    expiring_match_count INT,
    categories           TEXT[],
    meal_types           TEXT[]
)
LANGUAGE sql STABLE SECURITY INVOKER
SET search_path = public, pg_temp
AS $$
    WITH matched AS (
        SELECT
            r.id, r.title, r.image_url, r.preparation_time, r.difficulty,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id AND ingredient_id = ANY(p_pantry_roots))        AS available_count,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id)                                                 AS required_count,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id AND ingredient_id = ANY(p_expiring_roots))      AS expiring_match_count,
            COALESCE((SELECT array_agg(DISTINCT c.name)
                       FROM public.recipe_categories rc
                       JOIN public.categories c ON c.id = rc.category_id
                       WHERE rc.recipe_id = r.id), ARRAY[]::text[])                  AS categories,
            COALESCE((SELECT array_agg(DISTINCT meal_type) FROM public.recipe_meal_types
                       WHERE recipe_id = r.id), ARRAY[]::text[])                     AS meal_types
        FROM public.recipes r
        WHERE
            (p_query IS NULL OR r.title ILIKE '%' || p_query || '%' OR word_similarity(p_query, r.title) > 0.45)
            AND (array_length(p_difficulties, 1) IS NULL OR r.difficulty = ANY(p_difficulties))
            AND (array_length(p_time_windows, 1) IS NULL
                  OR ('quick'  = ANY(p_time_windows) AND r.preparation_time <= 15)
                  OR ('medium' = ANY(p_time_windows) AND r.preparation_time BETWEEN 16 AND 30)
                  OR ('long'   = ANY(p_time_windows) AND r.preparation_time > 30))
            AND (p_meal_type IS NULL OR EXISTS (
                    SELECT 1 FROM public.recipe_meal_types
                    WHERE recipe_id = r.id AND meal_type = p_meal_type))
            AND (array_length(p_categories, 1) IS NULL OR (
                    SELECT count(DISTINCT c.name)
                    FROM public.recipe_categories rc
                    JOIN public.categories c ON c.id = rc.category_id
                    WHERE rc.recipe_id = r.id AND c.name = ANY(p_categories)
                ) = array_length(p_categories, 1))
            AND (array_length(p_ingredient_roots, 1) IS NULL OR (
                    SELECT count(DISTINCT ingredient_id) FROM public.recipe_ingredients
                    WHERE recipe_id = r.id AND ingredient_id = ANY(p_ingredient_roots)
                ) = array_length(p_ingredient_roots, 1))
    )
    SELECT id, title, image_url, preparation_time, difficulty,
           available_count, required_count, expiring_match_count, categories, meal_types
    FROM matched
    WHERE available_count >= p_min_available
      AND expiring_match_count >= p_min_expiring
    ORDER BY
        CASE WHEN p_order_by = 'match'    THEN available_count::float / NULLIF(required_count, 0) END DESC NULLS LAST,
        CASE WHEN p_order_by = 'expiring' THEN expiring_match_count::float                        END DESC NULLS LAST,
        -- word_similarity only tells us a title is a good enough match, it does not say how good.
        -- Without this, two fuzzy matches would fall back straight to alphabetical order,
        -- which has nothing to do with how well they match the query.
        CASE WHEN p_query IS NOT NULL THEN
            GREATEST(
                CASE WHEN title ILIKE '%' || p_query || '%' THEN 1.0 ELSE 0 END, -- exact match
                word_similarity(p_query, title)
            )
        END DESC NULLS LAST,
        title ASC
    LIMIT p_limit;
$$;
