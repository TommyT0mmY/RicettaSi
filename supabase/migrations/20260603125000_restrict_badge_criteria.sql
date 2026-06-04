-- Supabase grants SELECT on all columns by default,
-- so the client could read badges.criteria, it's not a sensitive column but
-- it's better to lock it down to just the columns the UI actually shows.
-- evaluate_user_badges() still sees everything because it runs as
-- SECURITY DEFINER
REVOKE SELECT ON public.badges FROM anon, authenticated;
GRANT  SELECT (id, code, name, description) ON public.badges TO anon, authenticated;