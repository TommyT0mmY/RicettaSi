-- Local-only data, loaded by `supabase db reset`/`start` (see [db.seed] in
-- config.toml).
-- Never pushed to the remote project: `supabase db push` only applies
-- supabase/migrations.

-- Root ingredients
INSERT INTO public.ingredients (id, name, parent_ingredient_id) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'pomodoro', NULL),
    ('a0000000-0000-0000-0000-000000000002', 'pasta', NULL),
    ('a0000000-0000-0000-0000-000000000003', 'riso', NULL),
    ('a0000000-0000-0000-0000-000000000004', 'farina', NULL),
    ('a0000000-0000-0000-0000-000000000005', 'cioccolato', NULL),
    ('a0000000-0000-0000-0000-000000000006', 'pomodorino', 'a0000000-0000-0000-0000-000000000001');

-- Recipes covering all 3 difficulty values, all 3 time windows (quick/medium/long),
-- a recipe with null description/servings, and one with no ingredients/meal types/categories.
INSERT INTO public.recipes (id, title, description, preparation_time, difficulty, steps, servings) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Pasta al pomodoro',
     'Un primo piatto classico della cucina italiana.', 20, 'facile',
     '["Fai bollire l''acqua e cuoci la pasta", "Scalda il pomodoro in padella con aglio e olio", "Scola la pasta e uniscila al sugo"]',
     '4 persone'),
    ('b0000000-0000-0000-0000-000000000002', 'Risotto ai funghi',
     'Un risotto cremoso, cotto lentamente aggiungendo brodo poco a poco.', 35, 'medio',
     '["Tosta il riso a secco", "Aggiungi il brodo poco a poco mescolando", "Unisci i funghi gia saltati", "Manteca con burro e parmigiano"]',
     '2 persone'),
    ('b0000000-0000-0000-0000-000000000003', 'Pancake veloci',
     NULL, 10, 'facile',
     '["Mescola farina, latte e uova", "Cuoci le porzioni in padella antiaderente"]',
     NULL), -- No description, No servings
    ('b0000000-0000-0000-0000-000000000004', 'Torta al cioccolato',
     'Una torta golosa per le occasioni speciali.', 90, 'difficile',
     '["Sciogli il cioccolato a bagnomaria", "Monta le uova con lo zucchero", "Unisci la farina setacciata", "Versa in una tortiera imburrata", "Cuoci in forno e lascia raffreddare"]',
     '8 fette'),
    ('b0000000-0000-0000-0000-000000000005', 'Empty recipe',
     NULL, 1, 'facile', '[]', NULL), -- No steps, No description, No servings
    ('b0000000-0000-0000-0000-000000000006', 'Carbonara cream',
     NULL, 25, 'medio', '[]', NULL), -- only "carbonara" overlaps with a "pasta carbonara" search
    ('b0000000-0000-0000-0000-000000000007', 'Pasta alla carbonara',
     NULL, 25, 'medio', '[]', NULL); -- closer match than the above, must rank above it

INSERT INTO public.recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', '400g'), -- pomodoro in Pasta al pomodoro
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002', '320g'), -- pasta in Pasta al pomodoro
    ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003', '320g'), -- riso in Risotto ai funghi
    ('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000004', '200g'), -- farina in Pancake veloci
    ('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000004', '250g'), -- farina in Torta al cioccolato
    ('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000005', '150g'); -- cioccolato in Torta al cioccolato

INSERT INTO public.recipe_meal_types (recipe_id, meal_type) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'pranzo'),     -- Pasta al pomodoro
    ('b0000000-0000-0000-0000-000000000001', 'cena'),       -- Pasta al pomodoro
    ('b0000000-0000-0000-0000-000000000002', 'cena'),       -- Risotto ai funghi
    ('b0000000-0000-0000-0000-000000000003', 'colazione'),  -- Pancake veloci
    ('b0000000-0000-0000-0000-000000000003', 'brunch'),     -- Pancake veloci
    ('b0000000-0000-0000-0000-000000000004', 'merenda');    -- Torta al cioccolato

DELETE FROM public.categories;
INSERT INTO public.categories (id, name) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Italiana'),
    ('c0000000-0000-0000-0000-000000000002', 'In padella'),
    ('c0000000-0000-0000-0000-000000000003', 'Dolce'),
    ('c0000000-0000-0000-0000-000000000004', 'Gourmet');

INSERT INTO public.recipe_categories (recipe_id, category_id) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'), -- Pasta al pomodoro - Italiana
    ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002'), -- Pasta al pomodoro - In padella
    ('b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001'), -- Risotto ai funghi - Italiana
    ('b0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000003'), -- Torta al cioccolato - Dolce
    ('b0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004'); -- Torta al cioccolato - Gourmet
