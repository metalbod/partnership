# CLAUDE.md – transaction-profitshare-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Transaction` (with embedded `OfferingLine`s), `ProfitShareReport`. `profit_share_rule` is a legacy table, no longer read.
- A `Transaction` is a customer's subscription to a partner's WHOLE bundle: it holds
  the customer's name, phone and email, plus `partnerId`, `bundleId` and
  `ecoSystemId`. There is deliberately no offering or vendor on it. `partnerId` and
  `ecoSystemId` are denormalised so profit-share attribution doesn't require a
  cross-service call at report-generation time – keep that when extending the
  entity. (Legacy rows may still carry `offering_id`/`vendor_id`; the columns are
  nullable and unused.)
- Customer contact details are personal data. The data-residency analysis already
  treats this service as the most sensitive one; this raises it further.

## How a sale is priced and split
`TransactionService.record()` takes only the customer and a `subscriptionId`, then:
1. reads the partner programme (cost + partner share) from partner-subscription-service,
2. reads the bundle and each of its offerings (name, vendor, unit price) from
   ecosystem-bundle-service / vendor-offering-service (`RemoteCatalogClient`),
3. `PricingCalculator` splits the cost: each offering earns its unit price (FIXED
   amount, or PERCENTAGE of the cost), the partner earns its share, the company keeps
   the remainder; if the shares exceed the cost the sale is rejected (422),
4. stores the result on the `Transaction` – bundle name/version, one `OfferingLine`
   per offering (with the price terms and amount that vendor earned), partner and
   company amounts. That snapshot is never recomputed.
A programme in `PENDING_RECONSENT` still sells its current bundle version (consumers
stay on it until the partner re-consents); CANCELLED/SUPERSEDED programmes are refused.

`ProfitShareCalculationService` only aggregates the snapshots for a period into a
`ProfitShareReport` (totals + per-vendor earnings) and marks the transactions as
reported in the same DB transaction, so a re-run can't count a sale twice. Legacy
transactions without a snapshot (no `subscription_id`) are skipped.

Trade-off: recording a sale now needs the three upstream services to be up (502 if
not). Terms are only validated as a whole at sale time because the offerings' prices
live in another service.

## Known gaps in this scaffold
- Report export (CSV/PDF to S3, TDD Section 4.3) isn't built: a report holds totals and
  per-vendor earnings but there is no file export yet. The per-transaction snapshots
  are the source for it.
- Splitting a vendor's share when one vendor has several offerings in a bundle is just
  per-offering lines; no cross-vendor settlement logic exists (and must not: MVP is
  reporting-only).

## Do NOT do here
- Do not implement any payment/settlement logic in this service. MVP scope ends at
  "produce an exportable report" (BRD Section 6.1, TDD Section 12 future direction).

## Likely next tasks
- Add the report export (CSV to S3) over the stored snapshots.
- Wire the scheduled trigger (EventBridge Scheduler → Lambda → this service's
  batch endpoint, per TDD Section 4.4) instead of relying on the manual REST call.
- Revisit `api-contracts/transaction-recorded-event.schema.json` (still has
  `consumerExternalId`/`offeringId`) before the event is emitted.
