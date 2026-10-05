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
  flow, `offeringType` is informational only, never branch logic on it). Each
  offering carries the vendor's **unit price** inside a bundle, set when the
  offering is created: either a **FIXED** amount (MYR) or a **PERCENTAGE of the
  bundle cost**. This is what the vendor earns from each bundle sale.
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
- **Transaction** captures a customer (e.g. a student) subscribing, through a
  partner, to that partner's **whole bundle** – registered with the customer's
  name, contact number and email, plus partner and bundle. There is no offering
  (or vendor) on a transaction; customers never pick a single offering. When the
  bundle includes an insurance offering, `premium`, `sumInsured`, `policyNumber`
  are captured with it.
- **Partner programme terms** are fixed on the `PartnerSubscription` when the
  partner takes a bundle: the **cost of the bundle** for that partner (what each
  customer pays) and the **partner's share** of it (FIXED amount or PERCENTAGE of
  the cost). Vendors earn their offerings' unit prices; the **company keeps the
  remainder**. Terms where vendors + partner exceed the cost are rejected.
- **The split is snapshotted per transaction.** When a transaction is recorded the
  system resolves the subscription, the bundle's offerings and each offering's
  price, computes the Vendor / Partner / Company breakdown, and stores it on the
  transaction together with the list of offerings in the bundle at that moment.
  Later price or terms changes never rewrite past sales.
- **Profit-share reports** are produced **periodically by a scheduled batch job**
  that simply adds up those per-transaction snapshots (no per-transaction rule
  lookup, no rule fallback). MVP is **reporting-only – no payment/money movement
  in this codebase.**

## Personas / access control

- **Vendor** – no direct system access in MVP; managed by Admin.
- **Partner** – self-service via Partner Portal (own-organisation data only).
- **Consumer** – not a direct user of this platform.
- **Internal Admin** – full config access (vendors, eco-systems, bundles, partners,
  commercial terms); views all reports.
- **Internal Sales** – READ-ONLY. Sees vendor sign-up AND partner sign-up KPIs only
  (see `Partner.onboardedBy`). No configuration access, no commercial detail beyond
  their own KPI view. Do not give Sales write endpoints.

## Repository layout

This is a single monorepo (one root `.git`). It used to be eight separate
per-service/per-component repositories, merged in via `git subtree` with full
commit history preserved – look for the "Merge X repo into monorepo" commits
marking each seam. Note: `git log -- <path>` and `git blame` don't automatically
walk through those seams into a component's pre-merge history, since subtree
grafts each repo's existing commits in as-is rather than rewriting every
historical path with its new prefix – use `git log <merge-commit-hash>` to
browse a component's full history from its merge point backward.

```
partnership-pillar-platform/
├── services/
│   ├── vendor-offering-service/          (Java 21 / Spring Boot, port 8081)
│   ├── ecosystem-bundle-service/         (Java 21 / Spring Boot, port 8082)
│   ├── partner-subscription-service/     (Java 21 / Spring Boot, port 8083)
│   └── transaction-profitshare-service/  (Java 21 / Spring Boot, port 8084)
├── frontend/
│   ├── partner-portal/                   (placeholder – not yet scaffolded)
│   └── admin-console/                    (walking-skeleton Vite/React SPA)
├── api-contracts/                        (OpenAPI + event schema stubs for cross-pillar integration)
├── infra/                                (local Postgres/LocalStack bootstrap; AWS IaC not yet started, see TDD Section 9.2)
├── docs/                                 (BRD, SDD, TDD – source of truth for requirements)
└── docker-compose.yml                    (local Postgres + Redis + LocalStack for dev)
```

Services deliberately do NOT share a schema, call each other's internal Java code,
or JOIN across service boundaries. Cross-service references are UUIDs only (e.g.
`Bundle.offeringIds` holds `vendor-offering-service` Offering IDs with no JOIN/FK).
The one runtime dependency is `transaction-profitshare-service`, which reads the
partner's terms, the bundle and the offerings' prices over the other three
services' public REST APIs when a transaction is recorded (and snapshots them);
it never reads their data any other way.
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

- No authentication/authorization is wired up yet (target: Amazon Cognito with
  role-based claims per TDD Section 4.5).
- No Partner Portal frontend exists yet (Admin Console does – see below).
- No CI/CD, IaC, or actual AWS deployment – this is local-dev-ready only.
- Report export (CSV/PDF to S3) is not implemented – the batch job currently only
  computes splits in memory.

What's now working, beyond the initial scaffold:

- Bundle supersession publishes a real `BundleSuperseded` event to EventBridge
  (see `/api-contracts`), consumed by partner-subscription-service off SQS – not
  a manual REST call. Locally this runs against LocalStack (see
  `docker-compose.yml` + `infra/local/localstack-init.sh`). The manual
  `POST /v1/subscriptions/flag-pending-reconsent` endpoint still exists as a
  fallback only.
- Commercial terms drive profit-share: offerings have a unit price (fixed or % of
  bundle cost), partner programmes have a bundle cost and partner share, and every
  transaction records the offerings it covered and its Vendor/Partner/Company
  split at purchase (see `transaction-profitshare-service`'s `PricingCalculator`
  and `TransactionService`). This replaced the earlier profit-share rule
  fallback chain; the `profit_share_rule` table is legacy and unused.
- `frontend/admin-console` is a working walking-skeleton React SPA hitting all
  four services directly – see its own README for what's deliberately not
  there yet (auth, a transaction list, pagination).

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
