# TDD Summary – Partnership Pillar Platform

> Condensed reference. Full document: `Partnership_Pillar_TDD.docx` in this folder.

## Architecture style

Small, independently deployable services per bounded context (not a monolith, not
full microservices either) – 4 services, each right-sized for MVP cost, on AWS
ECS Fargate. Clean module boundaries mean each can scale/re-platform independently
later. A single-service "modular monolith" deployment is an explicit fallback
option if budget is tighter than expected – same code, different packaging.

## Target AWS stack (not yet built – this scaffold is local-dev only)

| Layer | Service |
|---|---|
| Compute | ECS Fargate (4 services) + Lambda (scheduled batch) |
| Data | RDS PostgreSQL (Multi-AZ) + ElastiCache Redis |
| Storage | S3 (reports, static assets) |
| Integration | API Gateway (REST), EventBridge (domain events), SQS |
| Identity | Cognito (Partner / Admin / Sales user pools, role-based claims) |
| Edge | CloudFront + WAF, Route 53, ACM |
| Region | `ap-southeast-5` (Asia Pacific / Malaysia) – data residency requirement |

## Data partitioning: one RDS database, one schema per service

The single RDS PostgreSQL instance (see Scalability table below) hosts **one**
database (`partnership`) shared by all four services – not four separate
databases, and not four separate RDS instances. Isolation between services is
enforced at the schema level instead:

- One schema per service (`vendoroffering`, `ecosystembundle`,
  `partnersubscription`, `transactionprofitshare`).
- One least-privilege DB role per service, each the owner of – and confined to –
  only its own schema. Postgres doesn't grant USAGE on a non-public schema to
  anyone but its owner by default, so a service's role genuinely cannot query
  another service's tables; this isn't just a naming convention.
- Per-service credentials issued via Secrets Manager (Section 5.1) exactly as if
  each had its own database – the credential-handling story doesn't change.
- A separate, read-only, cross-schema role exists for DBA/ops tooling only –
  never used by a service directly.

**Trade-off accepted for MVP:** a runaway query, connection-pool exhaustion, or
maintenance operation in one service's schema can still contend for the shared
instance's resources with the other three, in a way true per-service instances
would isolate away. This doesn't give up anything the MVP design wasn't already
trading off – a single RDS instance was already the target (see below) – and it
keeps operational overhead (backups, connection limits, patching) to one instance
instead of four.

## Network design

2-AZ VPC, 3 subnet tiers: public (ALB/NAT), private-app (ECS), private-data
(RDS/ElastiCache). VPC endpoints for S3/Secrets Manager/EventBridge/SQS/ECR to
avoid unnecessary public internet exposure.

## Security

Least-privilege IAM per service, encryption at rest (KMS) and in transit (TLS
1.2+), Secrets Manager for credentials, audit logging of config changes and
report generation. Compliance mapping: data residency (Malaysia), PDPA 2010,
BNM/FSA/IFSA (insurance policy-level data handling).

## Volumetrics (Year 1 planning assumptions)

5 eco-systems, 100 partners, 100,000 consumers, ~100k–200k transactions/month
peak. Indicative MVP AWS cost: **~$345–565/month** (order of magnitude, not a
quote – NAT Gateway data transfer flagged as the likely "hidden cost" to watch).

## Scalability & future evolution (MVP component → future direction)

| MVP | Future |
|---|---|
| Small per-module ECS Fargate tasks | EKS / larger Fargate fleet, per-service autoscaling |
| RDS single instance, Multi-AZ | Aurora PostgreSQL + read replicas |
| Periodic batch profit-share (Lambda) | Near-real-time stream processing |
| Manual settlement via exported reports | Dedicated ledger/settlement platform |
| Single AWS account | Multi-account via AWS Organizations |
| 2-AZ deployment | 3-AZ / cross-region DR |
| Cognito standalone | Federated with internal IdP (SAML/OIDC) |

## DR/HA targets (MVP)

RTO ≤ 4 hours, RPO ≤ 24 hours (targeting near-real-time via Multi-AZ sync).
Cross-region DR explicitly out of MVP scope (cost-effectiveness trade-off).

## What this means for the current scaffold

None of the above AWS infrastructure exists yet – see `/infra/README.md` for the
build-out plan. The 4 services currently run against local Postgres/Redis via
`docker-compose.yml`, structured so the eventual move to RDS/ElastiCache/ECS is a
configuration change (see each service's `application.yml`), not a rewrite.

The one-database/one-schema-per-service data partitioning above is already how
local dev works, not just the target design: `infra/local/init-databases.sql`
creates the single `partnership` database with one schema and one least-privilege
role per service, and each service connects via `?currentSchema=<service>` in its
JDBC URL. Moving to RDS changes the host/credentials, not the partitioning model.
