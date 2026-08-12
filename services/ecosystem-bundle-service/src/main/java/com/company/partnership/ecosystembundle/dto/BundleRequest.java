package com.company.partnership.ecosystembundle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BundleRequest(
        @NotNull UUID ecoSystemId,
        @NotBlank String name,
        @NotEmpty List<UUID> offeringIds
) {
}
