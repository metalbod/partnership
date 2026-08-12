-- Denormalises ecoSystemId onto transaction, same rationale as vendor_id/partner_id
-- (see CLAUDE.md): needed by ProfitShareCalculationService's eco-system-level rule
-- fallback tier without a cross-service call at report-generation time.
ALTER TABLE transaction ADD COLUMN eco_system_id UUID NOT NULL;

CREATE INDEX idx_transaction_eco_system_id ON transaction(eco_system_id);
