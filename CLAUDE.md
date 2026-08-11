# CLAUDE.md \u2013 ecosystem-bundle-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `EcoSystem`, `Bundle`.
- `Bundle.offeringIds` references `vendor-offering-service` Offering IDs by UUID
  only \u2013 there is intentionally no cross-service foreign key/JOIN. If you need
  offering details (name, vendor, type) when rendering a bundle, call
  vendor-offering-service's API rather than duplicating its data model here.

## The rule that matters most
`BundleService.createNewVersion()` is the ONLY sanctioned way to change a published
bundle's offering mix. If you find yourself adding an "update offerings" endpoint
that mutates a PUBLISHED bundle in place, stop \u2013 that violates BRD FR-BUN-03 and
breaks the partner re-consent guarantee in FR-BUN-04. Partners on the old version
must keep working against the old (locked) bundle until they explicitly re-consent.

## Integration stub (not yet wired)
When a bundle is superseded, `partner-subscription-service` needs to flag affected
subscriptions `PENDING_RECONSENT`. For MVP this is a manual/synchronous REST call
(`POST /v1/subscriptions/flag-pending-reconsent` on that service). The TDD's target
design is an EventBridge `BundleSuperseded` event instead \u2013 see `/api-contracts`
at the workspace root for the event shape to implement when ready.

## Likely next tasks
- Wire the EventBridge publish call in `createNewVersion()` (currently a TODO comment).
- Add a `GET /v1/eco-systems/{id}/bundles` convenience endpoint.
- Enforce `offeringIds` non-empty and de-duplicated at the service layer (currently
  only `@NotEmpty` at the DTO level).
