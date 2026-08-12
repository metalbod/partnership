package com.company.partnership.ecosystembundle.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors /api-contracts/bundle-superseded-event.schema.json exactly – keep the two
 * in sync if either changes; that file is the published contract of record.
 */
public record BundleSupersededEvent(
        String schemaVersion,
        UUID eventId,
        Instant occurredAt,
        UUID ecoSystemId,
        UUID oldBundleId,
        UUID newBundleId,
        int oldVersion,
        int newVersion
) {
    public static BundleSupersededEvent of(UUID ecoSystemId, UUID oldBundleId, UUID newBundleId,
                                            int oldVersion, int newVersion) {
        return new BundleSupersededEvent(
                "1.0", UUID.randomUUID(), Instant.now(), ecoSystemId, oldBundleId, newBundleId, oldVersion, newVersion);
    }
}
