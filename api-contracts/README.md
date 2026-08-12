# API Contracts – Cross-Pillar Integration

Published, versioned contracts between the Partnership Pillar and the other three
platforms (Value-Added Services, Loyalty, Store-Front/B2C), per SDD Section 6 and
TDD Section 7. MVP implements these as mocks/stubs – no live calls to the other
pillars yet, since those platforms don't exist. Building against these contracts
now means integration later is a wiring exercise, not a redesign.

| File | Contract | Direction |
|---|---|---|
| `consumer-enrolment-api.yaml` | Consumer Enrolment API | Store-Front → Partnership |
| `consumer-entitlement-api.yaml` | Consumer Entitlement Query API | Store-Front / Loyalty → Partnership |
| `transaction-recorded-event.schema.json` | Transaction domain event | Partnership → Loyalty |
| `bundle-superseded-event.schema.json` | Bundle supersession event | ecosystem-bundle-service → partner-subscription-service (internal, but same contract-first pattern) |

Breaking changes to any published contract require Architecture Review Board
notification (SDD Section 6.1).
