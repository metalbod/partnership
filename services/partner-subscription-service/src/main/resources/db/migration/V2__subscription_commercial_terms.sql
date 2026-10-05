-- Commercial terms per partner programme: the bundle's cost for this partner and the
-- partner's share (FIXED MYR or PERCENTAGE of that cost). Existing subscriptions start
-- at price 0 / share 0 until re-subscribed with real terms.
ALTER TABLE partner_subscription ADD COLUMN subscription_price     NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE partner_subscription ADD COLUMN partner_share_type     VARCHAR(20)   NOT NULL DEFAULT 'FIXED';
ALTER TABLE partner_subscription ADD COLUMN partner_share_value    NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE partner_subscription ALTER COLUMN subscription_price  DROP DEFAULT;
ALTER TABLE partner_subscription ALTER COLUMN partner_share_type  DROP DEFAULT;
ALTER TABLE partner_subscription ALTER COLUMN partner_share_value DROP DEFAULT;
