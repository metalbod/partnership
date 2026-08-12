package com.company.partnership.ecosystembundle.dto;

import com.company.partnership.ecosystembundle.domain.Bundle;

import java.util.List;
import java.util.UUID;

public record BundleResponse(
        UUID id, UUID ecoSystemId, String name, int version, UUID supersedesBundleId,
        Bundle.BundleStatus status, List<UUID> offeringIds
) {
    public static BundleResponse from(Bundle b) {
        return new BundleResponse(b.getId(), b.getEcoSystem().getId(), b.getName(), b.getVersion(),
                b.getSupersedesBundleId(), b.getStatus(), b.getOfferingIds());
    }
}
