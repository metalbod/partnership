# CLAUDE.md – transaction-profitshare-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Transaction`, `ProfitShareRule`, `ProfitShareReport`.
- A `Transaction` is a customer's subscription to a partner's WHOLE bundle: it holds
  the customer's name, phone and email, plus `partnerId`, `bundleId` and
  `ecoSystemId`. There is deliberately no offering or vendor on it. `partnerId` and
  `ecoSystemId` are denormalised so profit-share attribution doesn't require a
  cross-service call at report-generation time – keep that when extending the
  entity. (Legacy rows may still carry `offering_id`/`vendor_id`; the columns are
  nullable and unused.)
- Customer contact details are personal data. The data-residency analysis already
  treats this service as the most sensitive one; this raises it further.

## Rule resolution: exact → bundle-level → eco-system-level
`ProfitShareCalculationService.resolveRule()` tries, in order: an exact
bundle+partner rule (vendorId left null), then a bundle-level rule (only bundleId
set), then an eco-system-level rule (only ecoSystemId set). The old exact tier
matched vendor+bundle+partner; with whole-bundle transactions there is no single
vendor to match, so the vendor share is pooled per bundle rule. See `ProfitShareRuleRepository`'s query methods for
how each tier is matched, and `ProfitShareCalculationServiceTest` for coverage
of all three tiers plus the no-match failure case.

## Known gaps in this scaffold
- `runPeriodicCalculation()` computes splits per transaction but does not yet
  persist line items or export a file to S3 (TDD Section 4.3 – Reporting & Export
  Service). Add a `ProfitShareReportLine` entity and an S3 export step before this
  is usable end-to-end.
- No idempotency guard yet on the batch job (re-running for the same period could
  double-count if `includedInReportId` isn't set correctly) – verify the update to
  `Transaction.includedInReportId` happens atomically with report generation.

## Do NOT do here
- Do not implement any payment/settlement logic in this service. MVP scope ends at
  "produce an exportable report" (BRD Section 6.1, TDD Section 12 future direction).

## Likely next tasks
- Add the `ProfitShareReportLine` entity + CSV export to S3.
- Wire the scheduled trigger (EventBridge Scheduler → Lambda → this service's
  batch endpoint, per TDD Section 4.4) instead of relying on the manual REST call.
