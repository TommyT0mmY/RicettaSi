-- ============================================================
-- Gamification: badges with custom criteria (server-side)
--
-- Each badge has a server-only `criteria` JSONB field defining
-- the criteria for unlocking the badge.
-- ============================================================

-- Extra column on cooked_recipes: set to true when the cooking session
-- used at least one expiring pantry item (rescued_expiring badge).
ALTER TABLE public.cooked_recipes
    ADD COLUMN IF NOT EXISTS rescued_expiring BOOLEAN NOT NULL DEFAULT FALSE;

DELETE FROM public.badges;
INSERT INTO public.badges (code, name, description, criteria) VALUES
    ('primo-piatto',       'Primo piatto',            'Cucina la tua prima ricetta',                              '{"type":"cooked_total","value":1}'),
    ('cuoco-abituale',     'Cuoco abituale',           'Cucina 10 ricette',                                        '{"type":"cooked_total","value":10}'),
    ('repertorio-vario',   'Repertorio vario',         'Cucina 20 ricette diverse',                                '{"type":"cooked_unique","value":20}'),
    ('esploratore',        'Esploratore di sapori',    'Cucina ricette di 10 categorie diverse',                   '{"type":"categories_cooked","value":10}'),
    ('dispensa-piena',     'Dispensa piena',           'Aggiungi 20 ingredienti alla dispensa',                    '{"type":"pantry_added","value":20}'),
    ('collezionista',      'Collezionista',             'Salva 15 ricette tra i preferiti',                        '{"type":"favorites","value":15}'),
    ('esperto',            'Esperto ai fornelli',      'Raggiungi 500 XP',                                         '{"type":"xp","value":500}'),
    ('maestro',            'Maestro di cucina',        'Raggiungi 2000 XP',                                        '{"type":"xp","value":2000}'),
    ('salva-cibo',         'Salva-cibo',               'Cucina 5 ricette salvando ingredienti in scadenza',        '{"type":"rescued_expiring","value":5}'),
    ('gufo-fornelli',      'Gufo dei fornelli',        'Cucina 3 ricette in piena notte (tra le 23:00 e le 05:00)','{"type":"cooked_night","value":3}'),
    ('cucina-lampo',       'Cucina lampo',             'Cucina 10 ricette con preparazione in 15 minuti o meno',   '{"type":"cooked_quick","value":10,"max_minutes":15}'),
    ('colazione-campioni', 'Colazione dei campioni',   'Cucina 5 ricette da colazione',                            '{"type":"meal_type_cooked","value":5,"meal":"colazione"}');

-- ------------------------------------------------------------
-- Awards the user the badges they have earned.
-- For each badge we read its criteria ("type" and "value" fields),
-- count how many times the user has met that criteria, and if the
-- count reaches the threshold we insert the badge.
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.evaluate_user_badges(uid UUID)
RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE
    b         RECORD;
    type      TEXT;
    threshold    INT;
    v_count INT;
BEGIN
    -- no user
    IF NOT EXISTS (SELECT 1 FROM public.user_profiles WHERE user_id = uid) THEN
        RETURN;
    END IF;

    -- not yet unlocked badges
    FOR b IN SELECT id, criteria FROM public.badges WHERE id NOT IN (SELECT badge_id FROM public.user_badges WHERE user_id = uid) LOOP
        type      := b.criteria->>'type';
        threshold    := (b.criteria->>'value')::int;
        v_count := 0;

        IF type = 'xp' THEN
            SELECT xp INTO v_count
            FROM public.user_profiles
            WHERE user_id = uid;
        ELSIF type = 'cooked_total' THEN
            SELECT count(*) INTO v_count
            FROM public.cooked_recipes
            WHERE user_id = uid;
        ELSIF type = 'cooked_unique' THEN
            SELECT count(DISTINCT recipe_id) INTO v_count
            FROM public.cooked_recipes
            WHERE user_id = uid;
        ELSIF type = 'categories_cooked' THEN
            SELECT count(DISTINCT rc.category_id) INTO v_count
            FROM public.cooked_recipes cr
            JOIN public.recipe_categories rc ON rc.recipe_id = cr.recipe_id
            WHERE cr.user_id = uid;
        ELSIF type = 'pantry_added' THEN
            SELECT count(*) INTO v_count
            FROM public.pantry_items
            WHERE user_id = uid;
        ELSIF type = 'favorites' THEN
            SELECT count(*) INTO v_count
            FROM public.favorites
            WHERE user_id = uid;
        ELSIF type = 'rescued_expiring' THEN
            SELECT count(*) INTO v_count
            FROM public.cooked_recipes
            WHERE user_id = uid AND rescued_expiring = TRUE;
        ELSIF type = 'cooked_night' THEN
            SELECT count(*) INTO v_count
            FROM public.cooked_recipes
            WHERE user_id = uid
              AND (extract(hour FROM cooked_date AT TIME ZONE 'Europe/Rome') >= 23
                OR extract(hour FROM cooked_date AT TIME ZONE 'Europe/Rome') < 5);
        ELSIF type = 'cooked_quick' THEN
            SELECT count(*) INTO v_count
            FROM public.cooked_recipes cr
            JOIN public.recipes r ON r.id = cr.recipe_id
            WHERE cr.user_id = uid
              AND r.preparation_time <= (b.criteria->>'max_minutes')::int;
        ELSIF type = 'meal_type_cooked' THEN
            SELECT count(DISTINCT cr.recipe_id) INTO v_count
            FROM public.cooked_recipes cr
            JOIN public.recipe_meal_types rmt ON rmt.recipe_id = cr.recipe_id
            WHERE cr.user_id = uid
              AND rmt.meal_type = b.criteria->>'meal';
        END IF;

        IF v_count >= threshold THEN
            INSERT INTO public.user_badges (user_id, badge_id)
            VALUES (uid, b.id)
            ON CONFLICT (user_id, badge_id) DO NOTHING;
        END IF;
    END LOOP;
END;
$$;

-- Wrapper for triggers (they get the user from NEW/OLD)
CREATE OR REPLACE FUNCTION public.trg_evaluate_user_badges()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    PERFORM public.evaluate_user_badges(COALESCE(NEW.user_id, OLD.user_id));
    RETURN NULL;
END;
$$;

-- Re-evaluate after events that can change a stat
DROP TRIGGER IF EXISTS cooked_badges ON public.cooked_recipes;
CREATE TRIGGER cooked_badges
    AFTER INSERT ON public.cooked_recipes
    FOR EACH ROW EXECUTE FUNCTION public.trg_evaluate_user_badges();

DROP TRIGGER IF EXISTS pantry_badges ON public.pantry_items;
CREATE TRIGGER pantry_badges
    AFTER INSERT OR DELETE ON public.pantry_items
    FOR EACH ROW EXECUTE FUNCTION public.trg_evaluate_user_badges();

DROP TRIGGER IF EXISTS favorites_badges ON public.favorites;
CREATE TRIGGER favorites_badges
    AFTER INSERT OR DELETE ON public.favorites
    FOR EACH ROW EXECUTE FUNCTION public.trg_evaluate_user_badges();

-- XP changes (cooking awards XP) -> re-evaluate "xp" criteria
DROP TRIGGER IF EXISTS profile_xp_badges ON public.user_profiles;
CREATE TRIGGER profile_xp_badges
    AFTER UPDATE OF xp ON public.user_profiles
    FOR EACH ROW EXECUTE FUNCTION public.trg_evaluate_user_badges();

-- Unlocked badges are propagated to clients via data_version.
DROP TRIGGER IF EXISTS user_badges_data_version ON public.user_badges;
CREATE TRIGGER user_badges_data_version
    AFTER INSERT OR DELETE ON public.user_badges
    FOR EACH ROW EXECUTE FUNCTION public.bump_data_version();
