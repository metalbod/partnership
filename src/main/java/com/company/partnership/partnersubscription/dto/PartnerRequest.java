package com.company.partnership.partnersubscription.dto;

import com.company.partnership.partnersubscription.domain.Partner;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartnerRequest(
        @NotBlank String name,
        String contactEmail,
        String contactPhone,
        String commercialTerms,
        @NotNull Partner.OnboardedBy onboardedBy
) {
}
