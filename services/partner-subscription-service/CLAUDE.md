# CLAUDE.md – partner-subscription-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `Partner`, `PartnerSubscription`.
- `PartnerSubscription.bundleId` references `ecosystem-bundle-service` Bundle IDs
  by UUID only – no cross-service FK/JOIN.
- `Partner.onboardedBy` (ADMIN/SALES) exists specifically to support the Sales KPI
  requirement (FR-SAL-01/02: Sales sees read-only counts of vendors AND partners
  they signed up). Don't remove this field or its enum without checking the BRD.

## Commercial terms on the subscription
A `PartnerSubscription` carries the partner programme's terms: `subscriptionPrice` (the
bundle's cost for this partner's customers) and the partner's share
(`partnerShareType` FIXED/PERCENTAGE + `partnerShareValue`). Vendors' shares come from
the offerings' own unit prices and the company keeps the remainder – that final check
(shares ≤ cost) happens in transaction-profitshare-service at sale time, since the
offerings live in another service. Terms carry over unchanged on re-consent. Existing
subscriptions migrated with price 0 (can't take customers until re-subscribed with
terms); there's no edit-terms endpoint yet.

## Re-consent workflow – how it's wired
1. `ecosystem-bundle-service` supersedes a bundle (new version created) and
   publishes a `BundleSuperseded` event to EventBridge (see `/api-contracts`
   for the shape).
2. `event/BundleSupersededEventListener.java` here consumes it off the SQS
   queue an EventBridge rule routes it to, and calls
   `SubscriptionService.flagPendingReconsent(oldBundleId, newBundleId)`.
   Disabled under the `test` profile (see `@Profile("!test")` on the listener)
   since the Spring context test has no real queue to resolve.
3. `POST /v1/subscriptions/flag-pending-reconsent` still exists but is a
   manual/testing fallback only now – the listener is the primary path.
4. Consumers under a `PENDING_RECONSENT` subscription keep using the OLD bundle
   (don't change `bundleId` until `reconsent()` is called) – this is the guarantee
   BRD Section 4.2 makes to partners.

## Likely next tasks
- Add partner-facing "my subscriptions pending re-consent" query endpoint for the
  Partner Portal (FR-PTR-04/07).
- Add a uniqueness constraint: a partner should not hold two ACTIVE subscriptions
  to the same bundle lineage simultaneously (check before implementing – confirm
  with product whether re-subscribing after CANCELLED is a valid flow first).
