package com.company.partnership.partnersubscription.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors /api-contracts/bundle-superseded-event.schema.json exactly – keep the two
 * in sync if either changes; that file is the published contract of record. The
 * EventBridge rule that routes this to our SQS queue uses InputPath "$.detail", so
 * the SQS message body is exactly this shape (no EventBridge envelope wrapper).
 */
public record BundleSupersededEvent(
        String schemaVersion,
        UUID eventId,
        Instant occurredAt,
        UUID ecoSystemId,
        UUID oldBundleId,
        UUID newBundleId,
        Integer oldVersion,
        Integer newVersion
) {
}
