# Internal Admin & Sales Console (placeholder)

Not yet scaffolded. Per SDD Section 3.2 / TDD Section 2.2, target stack is a
React SPA, role-gated for two personas:

- **Admin:** full config access \u2013 vendors, offerings, eco-systems, bundles,
  partners, profit-share rules; views all reports (BRD FR-ADM-01..03).
- **Sales:** READ-ONLY vendor + partner sign-up KPI dashboards only
  (BRD FR-SAL-01..03). Do not expose config or commercial-detail screens to
  this role.

## Suggested next step
Decide whether this is a separate app from the Partner Portal or a single
role-gated app (still open per SDD Section 3.2) before scaffolding. Given the
very different permission surface (Admin needs config UI for all 4 backend
services), a separate app is likely simpler to build and reason about.
