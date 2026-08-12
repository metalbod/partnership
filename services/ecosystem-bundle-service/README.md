# Eco-System & Bundle Service

Owns EcoSystems and Bundles: mix-and-match packaging of vendor Offerings, with
locked composition per published version.

**Responsibility (SDD Section 3.1):** Eco-system creation; bundle composition;
bundle version locking and re-consent triggering.

**Covers BRD requirements:** FR-ECO-01, FR-ECO-02, FR-BUN-01 .. FR-BUN-05

## Run locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API docs: http://localhost:8082/swagger-ui.html

## Key endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /v1/eco-systems | Create an eco-system (FR-ECO-01) |
| POST | /v1/eco-systems/{id}/offerings/{offeringId} | Assign an offering to an eco-system |
| POST | /v1/bundles | Create a DRAFT bundle (FR-BUN-01) |
| POST | /v1/bundles/{id}/publish | Publish a bundle (locks its composition, FR-BUN-03) |
| POST | /v1/bundles/{id}/new-version | Create a new version of a published bundle (FR-BUN-04) |

## ⚠️ Critical business rule
Once a bundle is `PUBLISHED`, its `offeringIds` are **immutable**. Any change to the
offering mix MUST go through `POST /v1/bundles/{id}/new-version`, which creates a new
`Bundle` row and marks the old one `SUPERSEDED` – never `PUT`/mutate a published
bundle's offerings directly. See `BundleService` Javadoc for the full rationale.

See `CLAUDE.md` in this folder and the workspace-root `CLAUDE.md` before extending.
