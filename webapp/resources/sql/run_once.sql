-- Manually fixing surface-materials

-- Running tracks
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"gravel"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Sora';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"fiberglass"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Lasikuitu';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"brick-crush"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Tiilimurska';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"water"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Vesi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"concrete"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Betoni';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"textile"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Tekstiili';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"asphalt"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Asfaltti';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"ceramic"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Keraaminen';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"rock-dust"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Kivituhka';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"deinked-pulp"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Siistausmassa';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"stone"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Kivi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"metal"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Metalli';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"soil"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Maa';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"woodchips"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Hake';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"grass"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Nurmi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"synthetic"') WHERE document::json->'properties'->>'running-track-surface-material' LIKE 'Muovi / synteettinen%';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"sand"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Hiekka';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"artificial-turf"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Tekonurmi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"wood"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Puu';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"sawdust"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Sahanpuru';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, running-track-surface-material}', '"sand-infilled-artificial-turf"') WHERE document::json->'properties'->>'running-track-surface-material' = 'Hiekkatekonurmi';

-- Training spots
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"gravel"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Sora';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"fiberglass"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Lasikuitu';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"brick-crush"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Tiilimurska';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"water"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Vesi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"concrete"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Betoni';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"textile"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Tekstiili';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"asphalt"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Asfaltti';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"ceramic"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Keraaminen';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"rock-dust"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Kivituhka';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"deinked-pulp"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Siistausmassa';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"stone"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Kivi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"metal"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Metalli';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"soil"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Maa';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"woodchips"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Hake';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"grass"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Nurmi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"synthetic"') WHERE document::json->'properties'->>'training-spot-surface-material' LIKE 'Muovi / synteettinen%';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"sand"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Hiekka';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"artificial-turf"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Tekonurmi';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"wood"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Puu';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"sawdust"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Sahanpuru';
UPDATE public.sports_site SET document = jsonb_set(document, '{properties, training-spot-surface-material}', '"sand-infilled-artificial-turf"') WHERE document::json->'properties'->>'training-spot-surface-material' = 'Hiekkatekonurmi';