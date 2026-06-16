-- Two fixes in one:
-- 1. expiring sort was using a raw count, so a recipe with 3 expiring out of 10 total ranked
--    above one with 2 expiring out of 2 total. Switch to normalized ratios instead.
-- 2. when p_query is present, text relevance is now the primary sort key regardless of
--    p_order_by. match/expiring become secondary criteria. p_order_by default changed to
--    NULL since 'title' was never actually used as a value.
CREATE OR REPLACE FUNCTION public.search_recipes(
    p_query            TEXT    DEFAULT NULL,
    p_difficulties     TEXT[]  DEFAULT ARRAY[]::text[],
    p_time_windows     TEXT[]  DEFAULT ARRAY[]::text[],
    p_meal_type        TEXT    DEFAULT NULL,
    p_categories       TEXT[]  DEFAULT ARRAY[]::text[],  -- category names (categories.name), not UUIDs
    p_ingredient_roots UUID[]  DEFAULT ARRAY[]::uuid[],
    p_min_available    INT     DEFAULT 0,
    p_min_expiring     INT     DEFAULT 0,
    p_order_by         TEXT    DEFAULT NULL,              -- 'match' | 'expiring' | NULL
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
LANGUAGE sql VOLATILE SECURITY INVOKER
SET search_path = public, pg_temp
AS $$
    WITH
    pantry_roots AS (
        SELECT DISTINCT COALESCE(i.parent_ingredient_id, pi.ingredient_id) AS root_id
        FROM public.pantry_items pi
        JOIN public.ingredients i ON i.id = pi.ingredient_id
        WHERE pi.user_id = auth.uid() AND pi.consumed = false
    ),
    expiring_roots AS (
        SELECT DISTINCT COALESCE(i.parent_ingredient_id, pi.ingredient_id) AS root_id
        FROM public.pantry_items pi
        JOIN public.ingredients i ON i.id = pi.ingredient_id
        WHERE pi.user_id = auth.uid()
          AND pi.consumed = false
          AND pi.expiry_date IS NOT NULL
          AND pi.expiry_date <= current_date + 3
    ),
    matched AS (
        SELECT
            r.id, r.title, r.image_url, r.preparation_time, r.difficulty,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id AND ingredient_id IN (SELECT root_id FROM pantry_roots))    AS available_count,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id)                                                             AS required_count,
            (SELECT count(*)::int FROM public.recipe_ingredients
              WHERE recipe_id = r.id AND ingredient_id IN (SELECT root_id FROM expiring_roots))  AS expiring_match_count,
            COALESCE((SELECT array_agg(DISTINCT c.name)
                       FROM public.recipe_categories rc
                       JOIN public.categories c ON c.id = rc.category_id
                       WHERE rc.recipe_id = r.id), ARRAY[]::text[])                              AS categories,
            COALESCE((SELECT array_agg(DISTINCT meal_type) FROM public.recipe_meal_types
                       WHERE recipe_id = r.id), ARRAY[]::text[])                                 AS meal_types
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
        CASE WHEN p_query IS NOT NULL THEN
            GREATEST(
                CASE WHEN title ILIKE '%' || p_query || '%' THEN 1.0 ELSE 0 END,
                word_similarity(p_query, title)
            )
        END DESC NULLS LAST,
        CASE WHEN p_order_by = 'match'    THEN available_count::float / NULLIF(required_count, 0)      END DESC NULLS LAST,
        CASE WHEN p_order_by = 'match'    THEN available_count                                         END DESC NULLS LAST,
        CASE WHEN p_order_by = 'match'    THEN expiring_match_count                                    END DESC NULLS LAST,
        CASE WHEN p_order_by = 'expiring' THEN expiring_match_count::float / NULLIF(required_count, 0) END DESC NULLS LAST,
        CASE WHEN p_order_by = 'expiring' THEN available_count::float / NULLIF(required_count, 0)      END DESC NULLS LAST,
        title ASC
    LIMIT p_limit;
$$;

GRANT EXECUTE ON FUNCTION public.search_recipes(TEXT, TEXT[], TEXT[], TEXT, TEXT[], UUID[], INT, INT, TEXT, INT)
    TO authenticated;
