-- The vendor decides each offering's unit price inside a bundle: a FIXED amount (MYR)
-- or a PERCENTAGE of the bundle cost. Used for profit-share. Existing offerings start
-- at a fixed price of 0 until the vendor sets one.
ALTER TABLE offering ADD COLUMN price_type  VARCHAR(20)   NOT NULL DEFAULT 'FIXED';
ALTER TABLE offering ADD COLUMN price_value NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE offering ALTER COLUMN price_type  DROP DEFAULT;
ALTER TABLE offering ALTER COLUMN price_value DROP DEFAULT;
