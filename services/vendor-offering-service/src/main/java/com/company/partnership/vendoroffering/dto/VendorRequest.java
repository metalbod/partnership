package com.company.partnership.vendoroffering.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorRequest(
        @NotBlank String name,
        String contactEmail,
        String contactPhone,
        String commercialTerms
) {
}
