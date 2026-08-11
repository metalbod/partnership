CREATE TABLE eco_system (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    theme VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE eco_system_offering (
    eco_system_id UUID NOT NULL REFERENCES eco_system(id),
    offering_id UUID NOT NULL
);

CREATE TABLE bundle (
    id UUID PRIMARY KEY,
    eco_system_id UUID NOT NULL REFERENCES eco_system(id),
    name VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    supersedes_bundle_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE bundle_offering (
    bundle_id UUID NOT NULL REFERENCES bundle(id),
    offering_id UUID NOT NULL
);

CREATE INDEX idx_bundle_eco_system_id ON bundle(eco_system_id);
