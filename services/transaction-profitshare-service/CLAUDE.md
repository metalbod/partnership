# CLAUDE.md – transaction-profitshare-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Transaction`, `ProfitShareRule`, `ProfitShareReport`.
- `Transaction` denormalises `partnerId`, `vendorId`, and `ecoSystemId` (not just
  `offeringId` and `bundleId`) specifically so profit-share attribution doesn't
  require a cross-service call at report-generation time. Keep this
  denormalisation when extending the entity – it's intentional, not an oversight.

## Rule resolution: exact → bundle-level → eco-system-level
`ProfitShareCalculationService.resolveRule()` tries, in order: an exact
vendor+bundle+partner match, then a bundle-level rule (bundleId set, vendorId
and partnerId left null when the rule was configured), then an eco-system-level
rule (only ecoSystemId set). See `ProfitShareRuleRepository`'s query methods for
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
