# CLAUDE.md \u2013 transaction-profitshare-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Transaction`, `ProfitShareRule`, `ProfitShareReport`.
- `Transaction` denormalises `partnerId` and `vendorId` (not just `offeringId` and
  `bundleId`) specifically so profit-share attribution doesn't require a
  cross-service call at report-generation time. Keep this denormalisation when
  extending the entity \u2013 it's intentional, not an oversight.

## Known gaps in this scaffold (see TODOs in the code)
- `ProfitShareCalculationService.resolveRule()` only does an exact
  vendor+bundle+partner match. The intended design (SDD Section 5, "ProfitShareRule")
  is a fallback chain: exact match \u2192 bundle-level rule \u2192 eco-system-level rule.
  Implement the fallback before relying on this for anything beyond a single demo
  vendor/bundle/partner combination.
- `runPeriodicCalculation()` computes splits per transaction but does not yet
  persist line items or export a file to S3 (TDD Section 4.3 \u2013 Reporting & Export
  Service). Add a `ProfitShareReportLine` entity and an S3 export step before this
  is usable end-to-end.
- No idempotency guard yet on the batch job (re-running for the same period could
  double-count if `includedInReportId` isn't set correctly) \u2013 verify the update to
  `Transaction.includedInReportId` happens atomically with report generation.

## Do NOT do here
- Do not implement any payment/settlement logic in this service. MVP scope ends at
  "produce an exportable report" (BRD Section 6.1, TDD Section 12 future direction).

## Likely next tasks
- Implement the rule-resolution fallback chain described above.
- Add the `ProfitShareReportLine` entity + CSV export to S3.
- Wire the scheduled trigger (EventBridge Scheduler \u2192 Lambda \u2192 this service's
  batch endpoint, per TDD Section 4.4) instead of relying on the manual REST call.
