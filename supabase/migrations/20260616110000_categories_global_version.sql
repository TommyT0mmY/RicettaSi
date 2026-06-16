-- Categories are global shared data (like the global ingredient list) so changes
-- to them should invalidate the global_version counter, not data_version.
-- This trigger makes any admin insert/update/delete on `categories` bump the
-- same counter that ingredient changes use, so the client re-downloads categories
-- on the same pull cycle as global ingredients.

CREATE OR REPLACE FUNCTION public.bump_categories_global_version()
RETURNS TRIGGER
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
    UPDATE public.global_ingredients_version SET version = version + 1;
    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS categories_global_version ON public.categories;
CREATE TRIGGER categories_global_version
    AFTER INSERT OR UPDATE OR DELETE ON public.categories
    FOR EACH ROW EXECUTE FUNCTION public.bump_categories_global_version();
