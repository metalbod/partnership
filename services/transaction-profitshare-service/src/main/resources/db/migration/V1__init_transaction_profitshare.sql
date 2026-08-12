CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    consumer_enrolment_id UUID NOT NULL,
    offering_id UUID NOT NULL,
    bundle_id UUID NOT NULL,
    partner_id UUID NOT NULL,
    vendor_id UUID NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    is_insurance_offering BOOLEAN NOT NULL DEFAULT FALSE,
    premium NUMERIC(14,2),
    sum_insured NUMERIC(14,2),
    policy_number VARCHAR(100),
    transaction_timestamp TIMESTAMP NOT NULL DEFAULT now(),
    included_in_report_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transaction_pending ON transaction(included_in_report_id, transaction_timestamp);
CREATE INDEX idx_transaction_partner_id ON transaction(partner_id);
CREATE INDEX idx_transaction_vendor_id ON transaction(vendor_id);

CREATE TABLE profit_share_rule (
    id UUID PRIMARY KEY,
    eco_system_id UUID,
    bundle_id UUID,
    vendor_id UUID,
    partner_id UUID,
    vendor_share_pct NUMERIC(5,2) NOT NULL,
    company_share_pct NUMERIC(5,2) NOT NULL,
    partner_share_pct NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE profit_share_report (
    id UUID PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT now(),
    export_file_reference VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATED'
);
