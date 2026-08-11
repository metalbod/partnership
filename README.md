# Transaction & Profit-Share Service

Captures offering redemption/purchase transactions (including insurance
policy-level data) and runs the periodic, reporting-only profit-share calculation.

**Responsibility (SDD Section 3.1):** Captures transactions; periodic batch
calculation of profit-share splits.

**Covers BRD requirements:** FR-RPT-01 .. FR-RPT-07

## Run locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API docs: http://localhost:8084/swagger-ui.html

## Key endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /v1/transactions | Record a transaction (FR-RPT-01/02) |
| POST | /v1/profit-share/run?periodStart=...&periodEnd=... | Run the periodic calculation (local/manual trigger; production trigger is the scheduled AWS Lambda job per TDD 4.4) |

## \u26a0\ufe0f Critical business rules
- **MVP is reporting-only \u2013 no payment execution** (BRD FR-RPT-07). Do not add any
  code that moves money, calls a payment gateway, or marks a report "paid" \u2013 that
  is explicitly out of scope until the future ledger/settlement platform exists
  (see TDD Section 12).
- Insurance offerings must capture `premium`, `sumInsured`, `policyNumber` at the
  point of transaction (BRD Section 6.2) \u2013 these are transaction-time facts, not
  offering-level facts (which live in vendor-offering-service).

See `CLAUDE.md` in this folder and the workspace-root `CLAUDE.md` before extending.
