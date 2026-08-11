-- Creates one database per service, matching each service's application.yml
-- datasource URL (partnership_<service-shortname>). Runs automatically on first
-- `docker compose up` via the Postgres image's init-scripts mechanism.
CREATE DATABASE partnership_ecosystembundle;
CREATE DATABASE partnership_partnersubscription;
CREATE DATABASE partnership_transactionprofitshare;
-- partnership_vendoroffering is created by POSTGRES_DB in docker-compose.yml
