# CLAUDE.md – vendor-offering-service

Read the workspace-root `CLAUDE.md` first for the overall platform context. This
file adds service-specific notes.

## Scope of this service
- Entities: `Vendor`, `Offering`.
- Offerings are generic in MVP: `offeringType` (INSURANCE / NON_INSURANCE) is an
  informational tag only and must NOT branch business logic differently – this is
  an explicit BRD decision (Section 5.1), not an oversight.
- Offerings can exist with no eco-system/bundle assignment (FR-VEN-04). Do not add
  a NOT NULL foreign key from Offering to any eco-system/bundle concept – that
  association is owned entirely by `ecosystem-bundle-service` via offering IDs.

## Unit price (drives profit-share)
Every `Offering` has `priceType` + `priceValue`, set by the vendor at creation: `FIXED`
(a MYR amount) or `PERCENTAGE` (0–100, of the bundle cost). transaction-profitshare-service
reads this when a sale is recorded to work out the vendor's earnings; this service just
stores it (percentages above 100 are rejected). Existing offerings migrated to a fixed
price of 0 until a price is set. There is no edit endpoint for prices yet.

## Do NOT do here
- Do not model EcoSystem/Bundle in this service – that's `ecosystem-bundle-service`.
- Do not add insurance-specific fields (premium, sum insured, policy number) to
  `Offering` – those are transaction-time facts captured in
  `transaction-profitshare-service`, not offering-level facts.

## Likely next tasks
- Add pagination/filtering to `GET /v1/offerings` and `GET /v1/vendors` (FR-VEN-05).
- Add integration test hitting Testcontainers Postgres instead of H2, to catch
  Flyway/PostgreSQL-specific SQL issues before they surface in SIT.
- Add OpenAPI annotations (springdoc auto-generates from Spring MVC annotations;
  refine with `@Operation`/`@Schema` descriptions as the contract stabilises).
