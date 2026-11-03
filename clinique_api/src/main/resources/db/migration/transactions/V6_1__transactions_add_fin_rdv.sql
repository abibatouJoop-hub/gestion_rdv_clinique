ALTER TABLE rendez_vous ADD COLUMN fin TIMESTAMP;

UPDATE rendez_vous SET fin = debut + INTERVAL '30 minutes' WHERE fin IS NULL;

ALTER TABLE rendez_vous ALTER COLUMN fin SET NOT NULL;