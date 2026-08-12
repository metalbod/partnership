# Vendor & Offering Service

Owns Vendors and their Offerings (insurance or non-insurance – generic flow in MVP).

**Responsibility (SDD Section 3.1):** CRUD for vendors and their offerings; supports
offerings existing without eco-system/bundle assignment (BRD FR-VEN-04).

**Covers BRD requirements:** FR-VEN-01 .. FR-VEN-05

## Run locally

```bash
# Requires local Postgres (see /docker-compose.yml at the workspace root)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API docs (once running): http://localhost:8081/swagger-ui.html

## Key endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /v1/vendors | Onboard a vendor (FR-VEN-01) |
| GET | /v1/vendors | List vendors |
| GET | /v1/vendors/{id} | Get vendor |
| PUT | /v1/vendors/{id} | Edit vendor (FR-VEN-03) |
| DELETE | /v1/vendors/{id} | Deactivate vendor (FR-VEN-03) |
| POST | /v1/offerings | Create an offering under a vendor (FR-VEN-02) |
| GET | /v1/offerings?vendorId=... | List a vendor's offerings |

See `CLAUDE.md` in this folder and the workspace-root `CLAUDE.md` for full domain
context before extending this service.
