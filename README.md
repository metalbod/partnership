# Partnership Pillar Platform

Starter workspace for building the Partnership Pillar Platform in Claude Code,
based on the approved BRD, SDD and TDD (see `/docs`).

> **New to this workspace?** Open this folder in Claude Code – it auto-reads
> `CLAUDE.md` at this root for full project context (domain model, business
> rules, repo layout, tech stack, and what's scaffolded vs. still TODO).

## Quick start (local development)

1. **Start local infra:**
   ```bash
   docker compose up -d
   ```
   This starts Postgres (with one database per service) and Redis.

2. **Run a service:**
   ```bash
   cd services/vendor-offering-service
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   Repeat for the other three services (each on its own port – see table below).

3. **Explore the API:** each service exposes Swagger UI at
   `http://localhost:<port>/swagger-ui.html`.

| Service | Port | Responsibility |
|---|---|---|
| vendor-offering-service | 8081 | Vendors & their Offerings |
| ecosystem-bundle-service | 8082 | Eco-Systems & Bundles (incl. version locking) |
| partner-subscription-service | 8083 | Partners & whole-bundle Subscriptions (incl. re-consent) |
| transaction-profitshare-service | 8084 | Transaction capture & periodic profit-share calculation |

## Repository structure

This is a **multi-repo** workspace: each folder under `services/` is meant to
become (or already is) its own git repository. See `CLAUDE.md` for the full
layout and rationale.

## Documentation

- `docs/BRD-summary.md` + original `Partnership_Pillar_BRD.docx` – business
  requirements, domain model, commercial model, functional requirements.
- `docs/SDD-summary.md` + original `Partnership_Pillar_SDD.docx` – logical
  architecture, workflows, delivery plan, team/skillset, timeline.
- `docs/TDD-summary.md` + original `Partnership_Pillar_TDD.docx` – AWS technical
  architecture, network design, security, volumetrics, cost estimate, scale path.
- `api-contracts/` – OpenAPI specs and event schemas for the cross-pillar
  integration contracts referenced in the SDD/TDD.

## Status

Backend services are scaffolded with domain entities, business-rule-encoding
service layers, REST controllers and Flyway migrations – enough to run locally
and iterate on. Frontend, auth, CI/CD, IaC and AWS deployment are not yet
started. See `CLAUDE.md` \u2192 "What's scaffolded vs. what's still a TODO."
