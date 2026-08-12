# CLAUDE.md – Partnership Pillar Platform

This file is auto-loaded by Claude Code. Read it before making changes anywhere in
this workspace. Full detail lives in `/docs` (BRD, SDD, TDD) – this is the condensed
version for day-to-day development.

## What this is

The Partnership Pillar is one of four independent, API-integrated platforms in the
company's multi-pillar programme (Value-Added Services, **Partnership**, Loyalty,
Store-Front/B2C). It lets the company package insurance and non-insurance products
from third-party vendors into eco-systems and bundles, and distribute those bundles
commercially through partner organisations to reach large consumer populations.

**Illustrative example:** an "Education" eco-system bundles transportation, student
visa services, accommodation, telco SIM plans and insurance. A higher learning
institution (the partner) subscribes to a bundle; its students (consumers) access
the offerings. Vendor, company and partner share the commercial value on a
profit-share basis, computed periodically (not real-time) in MVP.

## Domain model (non-negotiable business rules)

- **Vendor** offers one or more **Offerings** (insurance or non-insurance – same
  flow, `offeringType` is informational only, never branch logic on it).
- **EcoSystem** is a themed collection of vendors/offerings (e.g. Education). Can
  exist with assigned offerings and zero bundles.
- **Bundle** is a mix-and-match of offerings within one eco-system.
  **Once published, a bundle's offering composition is LOCKED.** Any change =
  a new bundle version (`supersedesBundleId`), never an in-place mutation.
- **Partner** subscribes to a **whole bundle only** – no partial/offering-level
  subscription, ever. A different mix = a different bundle, not a partial pick.
- When a bundle is superseded, subscribed partners' `PartnerSubscription` rows
  flip to `PENDING_RECONSENT`. Their consumers keep using the OLD bundle version
  until the partner explicitly re-consents.
- **Consumer** enrolment/self-service UX belongs to the Store-Front (B2C) pillar,
  not this platform. This platform only exposes a Consumer Enrolment API contract
  and stores enrolment/usage records for profit-share attribution.
- **Transaction** captures an offering redemption/purchase. Insurance offerings
  additionally require `premium`, `sumInsured`, `policyNumber` at transaction time.
- **Profit-share** (Vendor / Company / Partner three-way split) is computed
  **periodically by a scheduled batch job**, not per-transaction, and MVP is
  **reporting-only – no payment/money movement in this codebase.**

## Personas / access control

- **Vendor** – no direct system access in MVP; managed by Admin.
- **Partner** – self-service via Partner Portal (own-organisation data only).
- **Consumer** – not a direct user of this platform.
- **Internal Admin** – full config access (vendors, eco-systems, bundles, partners,
  profit-share rules); views all reports.
- **Internal Sales** – READ-ONLY. Sees vendor sign-up AND partner sign-up KPIs only
  (see `Partner.onboardedBy`). No configuration access, no commercial detail beyond
  their own KPI view. Do not give Sales write endpoints.

## Repository layout

Each service below is its own git repository (not a monorepo) – see each folder's
own `.git`. This workspace folder is just a convenient local checkout location.

```
partnership-pillar-platform/
\u251c\u2500\u2500 services/
\u2502   \u251c\u2500\u2500 vendor-offering-service/          (Java 21 / Spring Boot, port 8081)
\u2502   \u251c\u2500\u2500 ecosystem-bundle-service/         (Java 21 / Spring Boot, port 8082)
\u2502   \u251c\u2500\u2500 partner-subscription-service/     (Java 21 / Spring Boot, port 8083)
\u2502   \u2514\u2500\u2500 transaction-profitshare-service/  (Java 21 / Spring Boot, port 8084)
\u251c\u2500\u2500 frontend/
\u2502   \u251c\u2500\u2500 partner-portal/                   (placeholder – not yet scaffolded)
\u2502   \u2514\u2500\u2500 admin-console/                    (placeholder – not yet scaffolded)
\u251c\u2500\u2500 api-contracts/                        (OpenAPI + event schema stubs for cross-pillar integration)
\u251c\u2500\u2500 infra/                                (IaC – not yet started, see TDD Section 9.2)
\u251c\u2500\u2500 docs/                                 (BRD, SDD, TDD – source of truth for requirements)
\u2514\u2500\u2500 docker-compose.yml                    (local Postgres + Redis for dev)
```

Services deliberately do NOT share a schema, call each other's internal Java code,
or JOIN across service boundaries. Cross-service references are UUIDs only (e.g.
`Bundle.offeringIds` holds `vendor-offering-service` Offering IDs with no JOIN/FK).
This mirrors the SDD's domain-oriented modularity principle: each service can be
extracted, rescaled or re-platformed independently as volumes grow (see TDD
Section 12).

They DO share a single Postgres database/RDS instance (`partnership`) as of the
schema-per-service data partitioning decision – see `docs/TDD-summary.md`'s "Data
partitioning" section. Isolation is enforced via one schema + one least-privilege
DB role per service, not via separate databases; a service's role has no grants
on another service's schema, so the "can't touch another service's data" boundary
holds even though the instance is shared.

## Tech stack (per TDD)

- **Backend:** Java 21, Spring Boot 3.3, Maven, PostgreSQL (Flyway migrations),
  Lombok, springdoc-openapi for Swagger UI.
- **Target infra (not yet built):** AWS ECS Fargate, RDS PostgreSQL (Multi-AZ),
  ElastiCache Redis, S3, API Gateway, EventBridge, SQS, Lambda (scheduled batch),
  Cognito, CloudFront/WAF – region `ap-southeast-5` (Malaysia). Full detail in
  `docs/TDD-summary.md` and the original TDD Word document.
- **Frontend (not yet built):** React SPA, one for Partner Portal, one for
  Internal Admin & Sales Console (or role-gated single app – still open, see SDD
  Section 3.2).

## What's scaffolded vs. what's still a TODO

Each service has entities, repositories, a service layer with the core business
rules encoded, REST controllers, Flyway migrations, and a basic Spring context
test. What's **not** done yet (see each service's own `CLAUDE.md` for specifics):

- Cross-service integration is stubbed as manual REST calls where the real design
  calls for EventBridge events (see `/api-contracts`) – e.g. bundle supersession
  should notify partner-subscription-service asynchronously, not via a manual
  endpoint.
- No authentication/authorization is wired up yet (target: Amazon Cognito with
  role-based claims per TDD Section 4.5).
- No frontend exists yet.
- No CI/CD, IaC, or actual AWS deployment – this is local-dev-ready only.
- Profit-share rule resolution only does exact-match; the designed fallback chain
  (exact \u2192 bundle-level \u2192 eco-system-level) is a TODO.
- Report export (CSV/PDF to S3) is not implemented – the batch job currently only
  computes splits in memory.

## Conventions

- Package root: `com.company.partnership.<service-shortname>` (replace `company`
  with the real org name when this becomes a real repo).
- REST paths are versioned: `/v1/...`.
- Every entity/service that encodes a BRD rule has a comment referencing the
  requirement ID (e.g. `// FR-BUN-03`) – keep this pattern so the connection to
  requirements stays traceable as the codebase grows.
- Don't add payment/settlement logic anywhere in MVP scope – flagged repeatedly
  in service-level CLAUDE.md files because it's the single most likely
  scope-creep trap given how naturally "profit-share" invites "now pay it out."

## Source documents

`/docs` contains the three governing documents. When in doubt about a requirement,
check the BRD (business rules), SDD (logical design/workflows), or TDD (technical/
AWS architecture) rather than re-deriving it from code.
