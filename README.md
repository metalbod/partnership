# API Contracts \u2013 Cross-Pillar Integration

Published, versioned contracts between the Partnership Pillar and the other three
platforms (Value-Added Services, Loyalty, Store-Front/B2C), per SDD Section 6 and
TDD Section 7. MVP implements these as mocks/stubs \u2013 no live calls to the other
pillars yet, since those platforms don't exist. Building against these contracts
now means integration later is a wiring exercise, not a redesign.

| File | Contract | Direction |
|---|---|---|
| `consumer-enrolment-api.yaml` | Consumer Enrolment API | Store-Front \u2192 Partnership |
| `consumer-entitlement-api.yaml` | Consumer Entitlement Query API | Store-Front / Loyalty \u2192 Partnership |
| `transaction-recorded-event.schema.json` | Transaction domain event | Partnership \u2192 Loyalty |
| `bundle-superseded-event.schema.json` | Bundle supersession event | ecosystem-bundle-service \u2192 partner-subscription-service (internal, but same contract-first pattern) |

Breaking changes to any published contract require Architecture Review Board
notification (SDD Section 6.1).
