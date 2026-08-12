# BRD Summary – Partnership Pillar Platform

> Condensed reference. Full document: `Partnership_Pillar_BRD.docx` in this folder.

## Business context

Four pillars: Value-Added Services (InsureConnect, built separately), **Partnership**
(this platform), Loyalty, Store-Front (B2C). Each is an independent platform,
API-integrated with the others.

## Domain model

- **Vendor** – third-party supplier of insurance or non-insurance **Offerings**.
  Offerings are generic in MVP (no differentiated flow by type).
- **Eco-System** – themed collection of vendors/offerings (Education, Automotive,
  Healthcare, Travel). Can exist with offerings assigned but no bundles.
- **Bundle** – mix-and-match of offerings within one eco-system. Any vendor/offering
  can appear in multiple bundles and eco-systems. **Composition is locked once
  published** – changes require a new bundle version + partner re-consent (each
  bundle can represent a separate contractual obligation).
- **Partner** – subscribes to a **whole bundle only** (no partial subscription); can
  subscribe to bundles across multiple eco-systems.
- **Consumer** – accesses bundle offerings via a partner; enrolled primarily via the
  Store-Front pillar (self-enrolment UX is out of scope here – API contract only).

## Actors

| Actor | Access |
|---|---|
| Vendor | No direct system access (MVP); managed by Admin |
| Partner | Partner Portal – own org's bundles, consumer summaries, reports |
| Consumer | Not a direct user of this platform |
| Internal Admin | Full config access; onboards vendors/eco-systems/bundles/partners; views all reports |
| Internal Sales | Read-only KPI: **vendor sign-up AND partner sign-up counts** (not just partners) |

## Commercial model – profit share

Three-way split: Vendor / Company (eco-system operator) / Partner. **MVP: computed
periodically (e.g. monthly) from transaction listings, NOT real-time.** No payment
execution in the platform – settlement happens externally using exported reports.
A separate future ledger/settlement platform is a possibility, not committed.

Insurance offerings require policy-level data (premium, sum insured, policy number)
captured at transaction time for profit-share calc.

## Scope – MVP (6 months)

**In:** vendor/offering mgmt, eco-system/bundle mgmt (incl. version locking),
partner mgmt + whole-bundle subscription + re-consent, partner portal, consumer
enrolment API (contract only), transaction capture, periodic profit-share
reporting, internal admin/sales functions, integration contracts (stubs) to the
other 3 pillars, AWS deployment with Malaysia data residency.

**Out:** actual payment/settlement, dedicated ledger platform, consumer-facing
enrolment UX (Store-Front owns it), integration with existing internal systems
(policy admin/CRM/ERP/payment gateway – manual for now), mid-flight bundle
composition changes (must version instead).

## Year 1 targets

5 eco-systems, 100 partners, 100,000 consumers, 50% YoY growth thereafter.

## Key functional requirement IDs (see full BRD for complete list)

- `FR-VEN-xx` – Vendor & Offering Management
- `FR-ECO-xx` / `FR-BUN-xx` – Eco-System & Bundle Management
- `FR-PTR-xx` – Partner Management & Portal
- `FR-CON-xx` – Consumer Enrolment (API)
- `FR-RPT-xx` – Profit-Share & Reporting
- `FR-ADM-xx` / `FR-SAL-xx` – Internal Admin & Sales
