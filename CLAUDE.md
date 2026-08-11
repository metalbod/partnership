# CLAUDE.md \u2013 partner-subscription-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Partner`, `PartnerSubscription`.
- `PartnerSubscription.bundleId` references `ecosystem-bundle-service` Bundle IDs
  by UUID only \u2013 no cross-service FK/JOIN.
- `Partner.onboardedBy` (ADMIN/SALES) exists specifically to support the Sales KPI
  requirement (FR-SAL-01/02: Sales sees read-only counts of vendors AND partners
  they signed up). Don't remove this field or its enum without checking the BRD.

## Re-consent workflow \u2013 how it should end up wired
1. `ecosystem-bundle-service` supersedes a bundle (new version created).
2. It should publish a `BundleSuperseded` event (see `/api-contracts` at the
   workspace root) carrying `{oldBundleId, newBundleId}`.
3. This service should have an event listener that calls
   `SubscriptionService.flagPendingReconsent(oldBundleId, newBundleId)`.
4. Until that listener exists, `POST /v1/subscriptions/flag-pending-reconsent` is
   a manual/synchronous stand-in \u2013 wire the real listener before UAT.
5. Consumers under a `PENDING_RECONSENT` subscription keep using the OLD bundle
   (don't change `bundleId` until `reconsent()` is called) \u2013 this is the guarantee
   BRD Section 4.2 makes to partners.

## Likely next tasks
- Add the EventBridge listener described above (replaces the manual endpoint).
- Add partner-facing "my subscriptions pending re-consent" query endpoint for the
  Partner Portal (FR-PTR-04/07).
- Add a uniqueness constraint: a partner should not hold two ACTIVE subscriptions
  to the same bundle lineage simultaneously (check before implementing \u2013 confirm
  with product whether re-subscribing after CANCELLED is a valid flow first).
