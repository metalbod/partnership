-- One database (partnership, created by POSTGRES_DB in docker-compose.yml) shared
-- by all four services, one schema per service instead of one database per
-- service. Isolation is preserved via a least-privilege role per service, each
-- owning only its own schema — a service's role genuinely cannot see another
-- service's schema (Postgres doesn't grant USAGE on a non-public schema to
-- anyone but its owner by default). See TDD-summary.md for the design rationale.
--
-- Runs automatically on first `docker compose up` via the Postgres image's
-- init-scripts mechanism, connected to the `partnership` database already.

CREATE ROLE vendoroffering_app WITH LOGIN PASSWORD 'changeme_local_only';
CREATE ROLE ecosystembundle_app WITH LOGIN PASSWORD 'changeme_local_only';
CREATE ROLE partnersubscription_app WITH LOGIN PASSWORD 'changeme_local_only';
CREATE ROLE transactionprofitshare_app WITH LOGIN PASSWORD 'changeme_local_only';

CREATE SCHEMA vendoroffering AUTHORIZATION vendoroffering_app;
CREATE SCHEMA ecosystembundle AUTHORIZATION ecosystembundle_app;
CREATE SCHEMA partnersubscription AUTHORIZATION partnersubscription_app;
CREATE SCHEMA transactionprofitshare AUTHORIZATION transactionprofitshare_app;

REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Read-only cross-schema access for the bootstrap admin user (POSTGRES_USER) only
-- – for local DBA tooling (pgweb, ad-hoc psql), not used by any service directly.
GRANT USAGE ON SCHEMA vendoroffering, ecosystembundle, partnersubscription, transactionprofitshare TO partnership_app;
GRANT SELECT ON ALL TABLES IN SCHEMA vendoroffering TO partnership_app;
GRANT SELECT ON ALL TABLES IN SCHEMA ecosystembundle TO partnership_app;
GRANT SELECT ON ALL TABLES IN SCHEMA partnersubscription TO partnership_app;
GRANT SELECT ON ALL TABLES IN SCHEMA transactionprofitshare TO partnership_app;

ALTER DEFAULT PRIVILEGES FOR ROLE vendoroffering_app IN SCHEMA vendoroffering GRANT SELECT ON TABLES TO partnership_app;
ALTER DEFAULT PRIVILEGES FOR ROLE ecosystembundle_app IN SCHEMA ecosystembundle GRANT SELECT ON TABLES TO partnership_app;
ALTER DEFAULT PRIVILEGES FOR ROLE partnersubscription_app IN SCHEMA partnersubscription GRANT SELECT ON TABLES TO partnership_app;
ALTER DEFAULT PRIVILEGES FOR ROLE transactionprofitshare_app IN SCHEMA transactionprofitshare GRANT SELECT ON TABLES TO partnership_app;
