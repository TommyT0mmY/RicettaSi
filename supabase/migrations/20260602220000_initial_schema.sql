-- ============================================================
--  * Two kinds of data live together: a shared catalog (recipes and the global
--    ingredient list) readable by everyone, and per-user data (pantry, favorites,
--    cooked recipes, badges) visible only to its owner.
--  * Sync is pull-based, with no Realtime: the client keeps a few version counters
--    and re-downloads a dataset only when its counter changed (see get_sync_versions
--    and the *_version bump triggers).
--  * Ingredients use a self-referencing synonym model, so different names for the
--    same thing match the same recipes (see the ingredients table).
--  * Pantry availability is binary: for recipe matching only "is the ingredient
--    there?" matters; quantity and expiry are informational.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Counter for the shared ingredient catalog (ingredients with created_by_user = false).
-- The client stores the value it last saw and re-downloads the catalog only
-- when this counter changed.
CREATE TABLE IF NOT EXISTS public.global_ingredients_version (
    id      BOOLEAN PRIMARY KEY DEFAULT true,  -- single row table
    version BIGINT  NOT NULL DEFAULT 0,
    CONSTRAINT global_ingredients_version_singleton CHECK (id)
);
INSERT INTO public.global_ingredients_version (id, version) VALUES (true, 0) ON CONFLICT (id) DO NOTHING;

-- Ingredients are stored once, in canonical singular form (name is UNIQUE).
-- Synonyms use a self-FK: a synonym row points to its representative through
-- parent_ingredient_id, and that representative (the "root") has parent NULL.
-- Recipe matching always resolves an ingredient to its root, so every variant
-- (e.g. "cocomero" -> "anguria") matches the same recipes; there is no group table.
-- created_by_user/user_id separate the shared catalog (false/NULL) from private
-- user-created rows.
CREATE TABLE IF NOT EXISTS public.ingredients (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  TEXT NOT NULL,
    parent_ingredient_id  UUID REFERENCES public.ingredients(id) ON DELETE SET NULL,
    created_by_user       BOOLEAN NOT NULL DEFAULT false,
    user_id               UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ingredients_name_unique UNIQUE (name)
);
CREATE INDEX IF NOT EXISTS idx_ingredients_user ON public.ingredients (user_id) WHERE created_by_user;  -- to query personal ingredients

CREATE TABLE IF NOT EXISTS public.recipes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               TEXT NOT NULL,
    description         TEXT,
    image_url           TEXT,
    preparation_time    INTEGER,
    difficulty          TEXT NOT NULL DEFAULT 'facile' CHECK (difficulty IN ('facile','medio','difficile')),
    steps               JSONB NOT NULL DEFAULT '[]'::jsonb,   -- ordered list of preparation steps
    created_by_user_id  UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_recipes_title_trgm ON public.recipes USING gin (title gin_trgm_ops);  -- to search by title

-- M2M recipe <-> meal type
CREATE TABLE IF NOT EXISTS public.recipe_meal_types (
    recipe_id  UUID NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    meal_type  TEXT NOT NULL CHECK (meal_type IN ('colazione','pranzo','cena','brunch','merenda','aperitivo')),
    PRIMARY KEY (recipe_id, meal_type)
);
CREATE INDEX IF NOT EXISTS idx_recipe_meal_types_meal_type_recipe ON public.recipe_meal_types (meal_type, recipe_id);

-- Global catalog of recipe categories. name is the canonical display label (e.g. 'Italiana').
CREATE TABLE IF NOT EXISTS public.categories (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  TEXT NOT NULL,
    CONSTRAINT categories_name_unique UNIQUE (name)
);

-- Seed the standard category set used by the Esplora filter chips.
INSERT INTO public.categories (name) VALUES
    ('Al forno'), ('In padella'), ('Senza cottura'), ('Al vapore'), ('Fritto'), ('Alla griglia'),
    ('Cottura lenta'), ('Fit & Wellness'), ('Per Bambini'), ('Da Condividere'), ('Romantico'),
    ('Schiscetta'), ('Cocktail'), ('Centrifugati e Smoothie'),
    ('Italiana'), ('Messicana'), ('Giapponese'), ('Cinese'), ('Indiana'), ('Thailandese'),
    ('Spagnola'), ('Greca'), ('Francese'), ('Americana'), ('Araba'), ('Fusion'), ('Regionale'),
    ('Mediterranea'), ('Vegetariana'), ('Vegana'), ('Senza Glutine'), ('Senza Lattosio'),
    ('Light'), ('Chetogenica'), ('Proteica'), ('Salutista'), ('Low Carb'),
    ('Dolce'), ('Comfort'), ('Pescatariana'), ('Frutta'), ('Economica'), ('Gourmet'),
    ('Grandi Occasioni'), ('Estiva'), ('Invernale'), ('Autunnale'), ('Primaverile'),
    ('Piccante'), ('Finger Food'), ('Al Cucchiaio')
ON CONFLICT (name) DO NOTHING;

-- M2M recipe <-> category (references the categories table by id).
CREATE TABLE IF NOT EXISTS public.recipe_categories (
    recipe_id   UUID NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES public.categories(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, category_id)
);
CREATE INDEX IF NOT EXISTS idx_recipe_categories_category_recipe ON public.recipe_categories (category_id, recipe_id);

-- The ingredients a recipe needs. quantity is descriptive text ("200g", "q.b.") and is
-- never used to check feasibility; availability is binary (present/absent in the pantry).
-- ingredient_id MUST be a root ingredient (parent_ingredient_id IS NULL in the ingredients table).
-- The check_recipe_ingredient_is_root trigger enforces this: inserts/updates with a non-root
-- ingredient_id are rejected with an error. This allows search_recipes to compare recipe
-- ingredient ids directly against pantry root ids, with no synonym resolution inside the query.
CREATE TABLE IF NOT EXISTS public.recipe_ingredients (
    recipe_id      UUID NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    ingredient_id  UUID NOT NULL REFERENCES public.ingredients(id) ON DELETE CASCADE,  -- must be a root (parent_ingredient_id IS NULL)
    quantity       TEXT,
    PRIMARY KEY (recipe_id, ingredient_id)
);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_ingredient_recipe ON public.recipe_ingredients (ingredient_id, recipe_id);

-- One row per user. xp/level and the two *_version counters are values
-- maintained by triggers (below); the client only ever reads them. ingredients_version
-- tracks the user's personal ingredients, data_version tracks pantry, favorites,
-- cooked recipes and badges.
CREATE TABLE IF NOT EXISTS public.user_profiles (
    user_id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name         TEXT NOT NULL DEFAULT '',
    avatar_emoji         TEXT NOT NULL DEFAULT '👨‍🍳',
    xp                   INTEGER NOT NULL DEFAULT 0,
    level                INTEGER NOT NULL DEFAULT 1,
    ingredients_version  BIGINT NOT NULL DEFAULT 0,     -- to sync personal ingredients list
    data_version         BIGINT NOT NULL DEFAULT 0,     -- to sync pantry, favorites, cooked recipes, badges
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One row per pantry item. The same ingredient can appear several times: rows are
-- never merged (two additions = two rows) and availability is binary. Only whether an
-- ingredient is present matters for matching, not its quantity. quantity and expiry_date
-- are informational; consumed/consumed_date move an item to the "finished" list instead
-- of deleting it. Consumed items are not taken into account when computing availability.
CREATE TABLE IF NOT EXISTS public.pantry_items (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    ingredient_id  UUID NOT NULL REFERENCES public.ingredients(id),
    quantity       TEXT,
    expiry_date    DATE,
    added_date     TIMESTAMPTZ NOT NULL DEFAULT now(),
    consumed       BOOLEAN NOT NULL DEFAULT false,
    consumed_date  TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_pantry_items_user_consumed_expiry ON public.pantry_items (user_id, consumed, expiry_date);  -- to query expiring items

CREATE TABLE IF NOT EXISTS public.favorites (
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    recipe_id   UUID NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    saved_date  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, recipe_id)
);

-- Each row = a single cooking event (the same recipe can appear multiple times).
CREATE TABLE IF NOT EXISTS public.cooked_recipes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    recipe_id    UUID NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    cooked_date  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cooked_user ON public.cooked_recipes (user_id, cooked_date DESC);  -- to query last cooked recipes

CREATE TABLE IF NOT EXISTS public.badges (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),    -- identity in the DB, referenced by foreign keys (user_badges.badge_id)
    code         TEXT NOT NULL,                                 -- readable key (e.g. 'primo-piatto'): the app uses it to pick the icon
    name         TEXT NOT NULL,                                 -- label shown to the user in the UI
    description  TEXT NOT NULL,
    criteria     JSONB NOT NULL                                 -- unlock rule in JSON (e.g. {"type":"xp","value":500}): read only by the server, never sent to the client
);
CREATE UNIQUE INDEX IF NOT EXISTS badges_code_unique ON public.badges (code);

-- M2M user <-> badge
CREATE TABLE IF NOT EXISTS public.user_badges (
    user_id        UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    badge_id       UUID NOT NULL REFERENCES public.badges(id) ON DELETE CASCADE,
    unlocked_date  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, badge_id)
);

-- ============================================================
-- FUNCTIONS & TRIGGERS
-- ============================================================

-- Creates the user profile upon registration. SECURITY DEFINER because the new
-- user does not have permissions on user_profiles yet.
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    INSERT INTO public.user_profiles (user_id, display_name)
    VALUES (NEW.id, split_part(NEW.email, '@', 1));             -- the email username is used as the user's display name
    RETURN NEW;
END;
$$;

-- After trigger to create the user profile upon registration.
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Bump ingredient version (handles both global and personal ingredients)
CREATE OR REPLACE FUNCTION public.bump_ingredient_version()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    v_created_by_user  BOOLEAN;
    v_user_id          UUID;
BEGIN
    v_created_by_user := COALESCE(NEW.created_by_user, OLD.created_by_user);
    v_user_id         := COALESCE(NEW.user_id, OLD.user_id);
    IF v_created_by_user THEN
        UPDATE public.user_profiles
           SET ingredients_version = ingredients_version + 1
         WHERE user_id = v_user_id;
    ELSE
        UPDATE public.global_ingredients_version SET version = version + 1;
    END IF;
    RETURN NULL;
END;
$$;
DROP TRIGGER IF EXISTS ingredients_version_bump ON public.ingredients;
CREATE TRIGGER ingredients_version_bump
    AFTER INSERT OR UPDATE OR DELETE ON public.ingredients
    FOR EACH ROW EXECUTE FUNCTION public.bump_ingredient_version();

-- Personal data version bump (pantry edits, favorites, cooked recipes)
CREATE OR REPLACE FUNCTION public.bump_data_version()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    uid UUID;
BEGIN
    uid := COALESCE(NEW.user_id, OLD.user_id);
    UPDATE public.user_profiles SET data_version = data_version + 1 WHERE user_id = uid;
    RETURN NULL;
END;
$$;
-- Bump on pantry change
DROP TRIGGER IF EXISTS pantry_data_version ON public.pantry_items;
CREATE TRIGGER pantry_data_version
    AFTER INSERT OR UPDATE OR DELETE ON public.pantry_items
    FOR EACH ROW EXECUTE FUNCTION public.bump_data_version();
-- Bump on favorites change
DROP TRIGGER IF EXISTS favorites_data_version ON public.favorites;
CREATE TRIGGER favorites_data_version
    AFTER INSERT OR UPDATE OR DELETE ON public.favorites
    FOR EACH ROW EXECUTE FUNCTION public.bump_data_version();
-- Bump on cooked recipes change
DROP TRIGGER IF EXISTS cooked_data_version ON public.cooked_recipes;
CREATE TRIGGER cooked_data_version
    AFTER INSERT OR UPDATE OR DELETE ON public.cooked_recipes
    FOR EACH ROW EXECUTE FUNCTION public.bump_data_version();

-- Keeps consumed_date aligned with the consumed flag, so the app never has to set it.
-- When an item is marked as consumed, the date is set to the current timestamp;
-- when it goes back to not consumed, the date is cleared to NULL.
CREATE OR REPLACE FUNCTION public.maintain_consumed_date()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.consumed AND NOT COALESCE(OLD.consumed, false) THEN
        NEW.consumed_date := now();
    ELSIF NOT NEW.consumed THEN
        NEW.consumed_date := NULL;
    END IF;
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS pantry_consumed_date ON public.pantry_items;
CREATE TRIGGER pantry_consumed_date
    BEFORE INSERT OR UPDATE ON public.pantry_items
    FOR EACH ROW EXECUTE FUNCTION public.maintain_consumed_date();

-- Rejects inserts/updates on recipe_ingredients where ingredient_id is not a root
-- (i.e. the ingredient has a parent). Recipes MUST ALWAYS reference root ingredients.
CREATE OR REPLACE FUNCTION public.check_recipe_ingredient_is_root()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.ingredients WHERE id = NEW.ingredient_id AND parent_ingredient_id IS NOT NULL) THEN
        RAISE EXCEPTION 'recipe_ingredients.ingredient_id must be a root ingredient, but % has a parent', NEW.ingredient_id;
    END IF;
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS recipe_ingredient_check_root ON public.recipe_ingredients;
CREATE TRIGGER recipe_ingredient_check_root
    BEFORE INSERT OR UPDATE ON public.recipe_ingredients
    FOR EACH ROW EXECUTE FUNCTION public.check_recipe_ingredient_is_root();

-- Rejects changes to ingredients.parent_ingredient_id that would turn a root into a child
-- if that ingredient is already referenced in recipe_ingredients. Together with the trigger
-- above, this guarantees that recipe ingredient ids are always roots for the lifetime of the data.
CREATE OR REPLACE FUNCTION public.check_ingredient_reparent()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.parent_ingredient_id IS NULL AND NEW.parent_ingredient_id IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM public.recipe_ingredients WHERE ingredient_id = NEW.id) THEN
            RAISE EXCEPTION 'cannot re-parent ingredient % because it is used in one or more recipes', NEW.id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS ingredient_check_reparent ON public.ingredients;
CREATE TRIGGER ingredient_check_reparent
    BEFORE UPDATE ON public.ingredients
    FOR EACH ROW EXECUTE FUNCTION public.check_ingredient_reparent();

-- Total xp needed to reach a given level. Each level requires more xp than the previous one.
CREATE OR REPLACE FUNCTION public.xp_for_level(p_level INTEGER)
RETURNS INTEGER
LANGUAGE sql IMMUTABLE AS $$
    SELECT 100 * (p_level - 1) * (p_level - 1);
$$;
-- Inverse of xp_for_level: which level a given amount of total xp falls into.
CREATE OR REPLACE FUNCTION public.level_for_xp(p_xp INTEGER)
RETURNS INTEGER
LANGUAGE sql IMMUTABLE AS $$
    SELECT floor(sqrt(p_xp::numeric / 100))::INTEGER + 1;
$$;

-- Awards xp after cooking a recipe.
-- The amount of given xp depends on the difficulty of the recipe.
CREATE OR REPLACE FUNCTION public.award_cooking_xp()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    v_difficulty  TEXT;
    v_reward      INTEGER;
BEGIN
    SELECT difficulty INTO v_difficulty FROM public.recipes WHERE id = NEW.recipe_id;
    v_reward := CASE v_difficulty
        WHEN 'difficile' THEN 30
        WHEN 'medio'     THEN 20
        ELSE 10
    END;
    UPDATE public.user_profiles
       SET xp    = xp + v_reward,
           level = public.level_for_xp(xp + v_reward)
     WHERE user_id = NEW.user_id;
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS cooked_recipe_xp ON public.cooked_recipes;
CREATE TRIGGER cooked_recipe_xp
    AFTER INSERT ON public.cooked_recipes
    FOR EACH ROW EXECUTE FUNCTION public.award_cooking_xp();

-- Returns the three sync counters in a single call. The client compares them with the
-- values it stored last time and re-downloads only the datasets whose counter changed.
CREATE OR REPLACE FUNCTION public.get_sync_versions()
RETURNS json
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp AS $$
    SELECT json_build_object(
        'global_version',   giv.version,
        'personal_version', profile.ingredients_version,
        'data_version',     profile.data_version
    )
    FROM public.global_ingredients_version giv, public.user_profiles profile
    WHERE profile.user_id = auth.uid();
$$;
GRANT EXECUTE ON FUNCTION public.get_sync_versions() TO authenticated;

-- ============================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================
-- Shared catalog (global ingredients, recipes and their M2M tables) is readable by
-- everyone, while writing it is left to the service_role/admin. Per-user tables are
-- readable and writable only by their owner, matched on auth.uid() = user_id.
ALTER TABLE public.global_ingredients_version ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ingredients        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recipes            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recipe_meal_types  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recipe_categories  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recipe_ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_profiles      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pantry_items       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cooked_recipes     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.badges             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_badges        ENABLE ROW LEVEL SECURITY;

CREATE POLICY "global_ingredients_version_read" ON public.global_ingredients_version
    FOR SELECT USING (true);

CREATE POLICY "ingredients_select" ON public.ingredients
    FOR SELECT USING (created_by_user = false OR user_id = auth.uid());

-- A user can only insert their own personal rows (global rows: admin only via service_role).
CREATE POLICY "ingredients_insert_own" ON public.ingredients
    FOR INSERT WITH CHECK (created_by_user = true AND user_id = auth.uid());

-- Public read; writes are reserved for service_role/admin.
CREATE POLICY "recipes_read_all"           ON public.recipes            FOR SELECT USING (true);
CREATE POLICY "recipe_meal_types_read_all" ON public.recipe_meal_types  FOR SELECT USING (true);
CREATE POLICY "categories_read_all"        ON public.categories         FOR SELECT USING (true);
CREATE POLICY "recipe_categories_read_all" ON public.recipe_categories  FOR SELECT USING (true);
CREATE POLICY "recipe_ingredients_read_all" ON public.recipe_ingredients FOR SELECT USING (true);

-- The user only sees and updates their own (the insert is done by the handle_new_user trigger).
CREATE POLICY "user_profiles_select_own" ON public.user_profiles
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "user_profiles_update_own" ON public.user_profiles
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "pantry_own" ON public.pantry_items
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "favorites_own" ON public.favorites
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "cooked_own" ON public.cooked_recipes
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "badges_read_all" ON public.badges
    FOR SELECT USING (true);

CREATE POLICY "user_badges_own" ON public.user_badges
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- RECIPE SEARCH RPC
-- ============================================================

-- Filters and ranks recipes against the user's pantry.
-- All ingredient ID parameters must be root IDs (parent_ingredient_id IS NULL).
-- The client is responsible for resolving any synonym to its root before calling this function.
-- Parameters:
--   p_query            free text matched against the recipe title (partial, case-insensitive)
--   p_difficulties     keep only recipes whose difficulty is one of these values (OR match; empty = any)
--   p_time_windows     keep only recipes that fall in one of these time buckets (OR match; empty = any):
--                        'quick'  -> preparation_time <= 15 min
--                        'medium' -> 16-30 min
--                        'long'   -> > 30 min
--   p_meal_type        keep only recipes tagged with this meal type; single value, null = any
--   p_categories       keep only recipes that have ALL of these categories (AND match; empty = any)
--                      example: ['italiana', 'vegana'] returns only italian-vegan recipes
--   p_ingredient_roots root IDs the recipe must contain ALL of (ingredient filter from Esplora)
--   p_pantry_roots     root IDs the user currently has in the pantry; used to compute available_count
--   p_expiring_roots   root IDs close to expiry in the pantry; used to compute expiring_match_count
--   p_min_available    discard recipes with fewer than this many pantry-available ingredients
--   p_min_expiring     discard recipes matching fewer than this many expiring ingredients
--   p_order_by         'title' (default) | 'match' (by pantry coverage %) | 'expiring' (by expiring ingredient count)
--   p_limit            maximum number of rows to return

-- Drop the old signature so it is not left as an unused overload.
DROP FUNCTION IF EXISTS public.search_recipes(TEXT, TEXT, INT, TEXT, TEXT, UUID[], UUID[], UUID[], INT, INT, TEXT, INT);

CREATE OR REPLACE FUNCTION public.search_recipes(
    p_query            TEXT    DEFAULT NULL,
    p_difficulties     TEXT[]  DEFAULT ARRAY[]::text[],  -- OR match: any difficulty in this list
    p_time_windows     TEXT[]  DEFAULT ARRAY[]::text[],  -- OR match: 'quick' | 'medium' | 'long'
    p_meal_type        TEXT    DEFAULT NULL,
    p_categories       TEXT[]  DEFAULT ARRAY[]::text[],  -- AND match: recipe must have ALL categories
    p_ingredient_roots UUID[]  DEFAULT ARRAY[]::uuid[],
    p_pantry_roots     UUID[]  DEFAULT ARRAY[]::uuid[],
    p_expiring_roots   UUID[]  DEFAULT ARRAY[]::uuid[],
    p_min_available    INT     DEFAULT 0,
    p_min_expiring     INT     DEFAULT 0,
    p_order_by         TEXT    DEFAULT 'title',           -- 'title' | 'match' | 'expiring'
    p_limit            INT     DEFAULT 60
)
RETURNS TABLE (
    id                   UUID,
    title                TEXT,
    description          TEXT,
    image_url            TEXT,
    preparation_time     INT,
    difficulty           TEXT,
    created_by_user_id   UUID,
    -- Match %: available_count / required_count * 100.
    -- Used to rank "Suggeriti per te" (home) and Esplora results: higher = more pantry coverage.
    available_count      INT,    -- how many of the recipe's ingredients are in the pantry
    required_count       INT,    -- total number of ingredients in the recipe (denominator for match %)
    -- Used to rank "Svuota il frigo" (home): higher = more expiring ingredients consumed by the recipe.
    expiring_match_count INT,    -- how many of the recipe's ingredients are expiring in the pantry
    categories           TEXT[],
    meal_types           TEXT[]
)
LANGUAGE sql STABLE SECURITY INVOKER SET search_path = public, pg_temp AS $$
    -- matched: one row per recipe that passes the filters, with pantry match counts.
    WITH matched AS (
        SELECT
            r.id, r.title, r.description, r.image_url,
            r.preparation_time, r.difficulty, r.created_by_user_id,
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
            (p_query IS NULL OR r.title ILIKE '%' || p_query || '%')
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
    SELECT id, title, description, image_url, preparation_time, difficulty, created_by_user_id,
           available_count, required_count, expiring_match_count, categories, meal_types
    FROM matched
    WHERE available_count >= p_min_available
      AND expiring_match_count >= p_min_expiring
    ORDER BY
        CASE WHEN p_order_by = 'match'    THEN available_count::float / NULLIF(required_count, 0) END DESC NULLS LAST,
        CASE WHEN p_order_by = 'expiring' THEN expiring_match_count::float                        END DESC NULLS LAST,
        title ASC
    LIMIT p_limit;
$$;

GRANT EXECUTE ON FUNCTION public.search_recipes(
    TEXT, TEXT[], TEXT[], TEXT, TEXT[], UUID[], UUID[], UUID[], INT, INT, TEXT, INT
) TO authenticated;
