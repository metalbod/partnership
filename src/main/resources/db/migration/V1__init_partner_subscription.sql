CREATE TABLE partner (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    commercial_terms VARCHAR(2000),
    onboarded_by VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE partner_subscription (
    id UUID PRIMARY KEY,
    partner_id UUID NOT NULL REFERENCES partner(id),
    bundle_id UUID NOT NULL,
    bundle_version_at_subscription INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    pending_bundle_id UUID,
    consented_at TIMESTAMP,
    reconsented_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_partner_subscription_partner_id ON partner_subscription(partner_id);
CREATE INDEX idx_partner_subscription_bundle_id ON partner_subscription(bundle_id);
