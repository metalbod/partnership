# Partner Portal (placeholder)

Not yet scaffolded. Per SDD Section 3.2 / TDD Section 2.2, target stack is a
React SPA served via CloudFront + S3, calling the four backend services'
`/v1/...` REST APIs through an API Gateway/ALB.

**Responsibility (BRD FR-PTR-04..07):** partner self-service view of subscribed
bundles, enrolled consumer summaries, profit-share/transaction reports, and the
bundle re-consent workflow (see `partner-subscription-service`'s
`POST /v1/subscriptions/{id}/reconsent`).

## Suggested next step
`npx create-vite@latest . -- --template react-ts` (or the org's standard React
starter, once decided) and wire up calls to `partner-subscription-service`
(port 8083) and `transaction-profitshare-service` (port 8084) first, since
those cover the two BRD-mandated portal views (subscriptions, reports).
