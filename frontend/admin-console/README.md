# Internal Admin & Sales Console

Walking-skeleton React SPA (Vite + TypeScript, no router/state library) that calls all
four Partnership Pillar backend services directly from the browser. Goal is proving
cross-service integration end-to-end, not full CRUD polish – see workspace-root
CLAUDE.md for the domain model this exercises.

**Responsibility (BRD FR-ADM-01..03):** full config access – vendors, offerings,
eco-systems, bundles, partners, profit-share reports.

## What's here

Four tabs, each talking to one or more of the backend services directly (ports
8081–8084, see `src/api.ts`):

- **Vendors & Offerings** – create vendors, create/list offerings per vendor.
- **Eco-Systems & Bundles** – create eco-systems, assign offerings, create/publish
  bundles, create new bundle versions (exercises the FR-BUN-03/04 supersession flow).
- **Partners & Subscriptions** – create partners, subscribe to a whole published
  bundle, re-consent a `PENDING_RECONSENT` subscription (the tail end of the
  EventBridge → SQS BundleSuperseded flow – see `ecosystem-bundle-service` and
  `partner-subscription-service` CLAUDE.md files).
- **Transactions & Profit-Share** – register a customer (name, contact, email) against
  a partner and the bundle they subscribed to as a whole, trigger the periodic
  profit-share calculation on demand (reporting-only – no payment execution).

## What's NOT here (by design, for now)

- **No auth/role-gating.** Cognito isn't wired up anywhere in the platform yet, so
  this shows the Admin's full surface to anyone who opens it. Don't treat this as a
  Sales-safe view.
- **No transaction list.** `transaction-profitshare-service` has no
  `GET /v1/transactions` list endpoint yet – the Transactions tab only shows what
  was recorded in the current browser session, not a persisted view.
- **No pagination, editing, or delete flows** beyond what each backend already
  exposes (e.g. Vendor has `PUT`/`DELETE`, not wired up here yet).

## Run locally

Requires all four backend services running locally (see workspace-root README /
each service's own `mvn spring-boot:run -Dspring-boot.run.profiles=local`) with
Postgres up. Each service needs its `CorsConfig` (added alongside this frontend) to
allow `http://localhost:5173` – that's local-dev-only; production fronts all four
services with a single API Gateway/ALB (TDD Section 4.1) instead.

```bash
npm install
npm run dev
```

Opens on http://localhost:5173.

## Suggested next steps

- Decide on the Admin vs. Sales split (still open per SDD Section 3.2) – Sales needs
  a much narrower, read-only KPI view, not this console.
- Add the transaction list endpoint backend-side, then wire it up here.
- Once Cognito exists, gate the whole app behind it.
