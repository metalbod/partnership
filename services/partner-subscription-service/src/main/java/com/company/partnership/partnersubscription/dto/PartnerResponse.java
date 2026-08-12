package com.company.partnership.partnersubscription.dto;

import com.company.partnership.partnersubscription.domain.Partner;

import java.util.UUID;

public record PartnerResponse(UUID id, String name, Partner.OnboardedBy onboardedBy, Partner.PartnerStatus status) {
    public static PartnerResponse from(Partner p) {
        return new PartnerResponse(p.getId(), p.getName(), p.getOnboardedBy(), p.getStatus());
    }
}
