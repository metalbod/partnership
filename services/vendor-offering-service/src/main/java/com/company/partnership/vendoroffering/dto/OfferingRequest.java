package com.company.partnership.vendoroffering.dto;

import com.company.partnership.vendoroffering.domain.Offering;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OfferingRequest(
        @NotNull UUID vendorId,
        @NotBlank String name,
        String description,
        @NotNull Offering.OfferingType offeringType
) {
}
