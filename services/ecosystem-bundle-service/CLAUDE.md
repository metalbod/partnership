# CLAUDE.md – ecosystem-bundle-service

Read the workspace-root `CLAUDE.md` first. This file adds service-specific notes.

## Scope of this service
- Entities: `EcoSystem`, `Bundle`.
- `Bundle.offeringIds` references `vendor-offering-service` Offering IDs by UUID
  only – there is intentionally no cross-service foreign key/JOIN. If you need
  offering details (name, vendor, type) when rendering a bundle, call
  vendor-offering-service's API rather than duplicating its data model here.

## The rule that matters most
`BundleService.createNewVersion()` is the ONLY sanctioned way to change a published
bundle's offering mix. If you find yourself adding an "update offerings" endpoint
that mutates a PUBLISHED bundle in place, stop – that violates BRD FR-BUN-03 and
breaks the partner re-consent guarantee in FR-BUN-04. Partners on the old version
must keep working against the old (locked) bundle until they explicitly re-consent.

## Integration: BundleSuperseded event
When a bundle is superseded, `createNewVersion()` publishes a `BundleSuperseded`
event to EventBridge (see `event/BundleSupersededEventPublisher.java`) after the
transaction commits – `partner-subscription-service` consumes it off SQS to flag
affected subscriptions `PENDING_RECONSENT`. See `/api-contracts` for the event
shape. Locally this runs against LocalStack (`docker-compose.yml` +
`infra/local/localstack-init.sh`); against real AWS it's the same
`EventBridgeClient` with no endpoint override (see `application.yml`'s
`aws.eventbridge` block). The old manual
`POST /v1/subscriptions/flag-pending-reconsent` endpoint on that service still
exists as a fallback only – don't rely on it as the primary path.

## Gotcha: @ElementCollection fetch type
`Bundle.offeringIds` and `EcoSystem.assignedOfferingIds` are `EAGER`, not the JPA
default `LAZY` – `open-in-view` is off, so a LAZY collection throws outside the
`@Transactional` service method the moment any controller tries to serialize it
(every read/publish/list endpoint hits this). Don't change these back to LAZY
without also fixing that.

## Likely next tasks
- Add a `GET /v1/eco-systems/{id}/bundles` convenience endpoint.
- Enforce `offeringIds` non-empty and de-duplicated at the service layer (currently
  only `@NotEmpty` at the DTO level).
