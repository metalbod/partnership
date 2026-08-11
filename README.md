# Partner & Subscription Service

Owns Partners and their whole-bundle Subscriptions, including the re-consent
workflow triggered when a subscribed bundle is superseded.

**Responsibility (SDD Section 3.1):** Partner onboarding; whole-bundle subscription;
subscription history; re-consent workflow orchestration.

**Covers BRD requirements:** FR-PTR-01 .. FR-PTR-08

## Run locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API docs: http://localhost:8083/swagger-ui.html

## Key endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /v1/partners | Onboard a partner (FR-PTR-01) |
| POST | /v1/subscriptions | Subscribe a partner to a whole bundle (FR-PTR-02/03) |
| POST | /v1/subscriptions/{id}/reconsent | Partner re-consents to a new bundle version (FR-PTR-07) |
| POST | /v1/subscriptions/flag-pending-reconsent | Internal: mark subscriptions pending re-consent (MVP stub for the BundleSuperseded event) |

## \u26a0\ufe0f Critical business rule
Subscriptions are **whole-bundle only** \u2013 there is deliberately no offering-level
selection field anywhere in this service's API. If a partner needs a different mix,
the answer is "ecosystem-bundle-service creates a new bundle", not "let them pick
offerings here." See BRD Section 5.1 and SDD Section 4.2.

See `CLAUDE.md` in this folder and the workspace-root `CLAUDE.md` before extending.
