# SDD Summary – Partnership Pillar Platform

> Condensed reference. Full document: `Partnership_Pillar_SDD.docx` in this folder.

## Design principles

- Contract-first integration with the other 3 pillars (define now, connect later).
- Domain-oriented modularity – services aligned to bounded contexts, independently
  scalable/extractable later.
- Cost-effective MVP, evolutionary architecture (see TDD Section 12 for the scale
  path this enables).
- Security/compliance by design (RBAC per persona, Malaysia data residency,
  auditability from day one).
- Configuration over code for eco-system/bundle/profit-share-rule setup.

## Logical architecture (4 core modules → maps 1:1 to the 4 backend services)

1. **Vendor & Offering Management** → `vendor-offering-service`
2. **Eco-System & Bundle Management** → `ecosystem-bundle-service`
3. **Partner & Subscription Management** → `partner-subscription-service`
4. **Transaction Capture & Profit-Share Engine** → `transaction-profitshare-service`

Plus: Consumer Enrolment API (stub), Reporting & Export Service, Integration &
Event Layer, Data Store – these are cross-cutting/secondary components, not
separate services in the MVP scaffold.

## Key workflows

1. **Vendor & Bundle Configuration** – onboard vendor → assign offerings to
   eco-system → create bundle (mix-and-match) → publish (locks composition).
2. **Partner Subscription & Re-consent** – onboard partner → subscribe to whole
   bundle → [if bundle superseded] flag PENDING_RECONSENT → partner re-consents.
3. **Transaction Capture & Profit-Share** – record transaction (+ policy data if
   insurance) → periodic batch calculates split → export report. No payment.
4. **Consumer Enrolment (API)** – Store-Front calls enrolment API → validate
   partner subscription is ACTIVE → create enrolment record → other pillars can
   query entitlements.

## Data model (conceptual entities → implemented as JPA entities per service)

Vendor, Offering, EcoSystem, Bundle, BundleOffering (junction), Partner,
PartnerSubscription, ConsumerEnrolment, Transaction, ProfitShareRule,
ProfitShareReport. See each service's domain package for the actual entities;
cross-service references are UUIDs only, never JOINs/FKs across service boundaries.

## Integration contracts (see /api-contracts)

| Contract | Direction | MVP status |
|---|---|---|
| Consumer Enrolment API | Store-Front → Partnership | Contract published, stub only |
| Consumer Entitlement Query API | Store-Front/Loyalty → Partnership | Contract published, stub only |
| Transaction Event | Partnership → Loyalty | Schema published, not emitted yet |
| Vendor/Offering Catalogue Sync | Partnership ↔ VAS (future) | Sketched only, not prioritised |

## Delivery approach (for reference – not applicable to Claude Code build order directly)

Agile Scrum, 2-week sprints, ~10–12 person team, 6-month MVP target (Sprint 0 setup
→ Sprints 1–10 feature build → go-live). See full SDD for the sprint-by-sprint
breakdown and team/skillset table if useful for planning parallel workstreams.
