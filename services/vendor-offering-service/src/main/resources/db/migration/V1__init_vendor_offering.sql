CREATE TABLE vendor (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    commercial_terms VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE offering (
    id UUID PRIMARY KEY,
    vendor_id UUID NOT NULL REFERENCES vendor(id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    offering_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_offering_vendor_id ON offering(vendor_id);
