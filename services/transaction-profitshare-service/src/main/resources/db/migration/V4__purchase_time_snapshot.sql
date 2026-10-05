-- Each transaction now snapshots, at purchase: the partner programme it was sold under, the
-- bundle (name/version) and every offering in it with the vendor's price terms and earnings,
-- plus the partner's and company's shares. Reports aggregate these snapshots.
-- Legacy rows (recorded before this) have no snapshot: shares default to 0 and no lines.
ALTER TABLE transaction ADD COLUMN subscription_id UUID;
ALTER TABLE transaction ADD COLUMN bundle_name VARCHAR(255);
ALTER TABLE transaction ADD COLUMN bundle_version INTEGER;
ALTER TABLE transaction ADD COLUMN partner_amount NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE transaction ADD COLUMN company_amount NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE transaction ALTER COLUMN partner_amount DROP DEFAULT;
ALTER TABLE transaction ALTER COLUMN company_amount DROP DEFAULT;

CREATE TABLE transaction_offering_line (
    transaction_id UUID NOT NULL REFERENCES transaction(id),
    offering_id    UUID NOT NULL,
    offering_name  VARCHAR(255) NOT NULL,
    vendor_id      UUID NOT NULL,
    price_type     VARCHAR(20) NOT NULL,
    price_value    NUMERIC(14,2) NOT NULL,
    amount         NUMERIC(14,2) NOT NULL
);
CREATE INDEX idx_transaction_offering_line_tx ON transaction_offering_line(transaction_id);
CREATE INDEX idx_transaction_offering_line_vendor ON transaction_offering_line(vendor_id);

ALTER TABLE profit_share_report ADD COLUMN transaction_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE profit_share_report ADD COLUMN total_amount   NUMERIC(16,2) NOT NULL DEFAULT 0;
ALTER TABLE profit_share_report ADD COLUMN vendor_total   NUMERIC(16,2) NOT NULL DEFAULT 0;
ALTER TABLE profit_share_report ADD COLUMN partner_total  NUMERIC(16,2) NOT NULL DEFAULT 0;
ALTER TABLE profit_share_report ADD COLUMN company_total  NUMERIC(16,2) NOT NULL DEFAULT 0;
-- profit_share_rule is no longer read: the breakdown now comes from the offerings' and the
-- partner programme's own terms. The table is left in place (not dropped) as legacy data.
