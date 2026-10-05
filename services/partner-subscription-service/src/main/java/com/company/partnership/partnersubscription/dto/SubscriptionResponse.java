package com.company.partnership.partnersubscription.dto;

import com.company.partnership.partnersubscription.domain.PartnerSubscription;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id, UUID partnerId, UUID bundleId, int bundleVersionAtSubscription,
        PartnerSubscription.SubscriptionStatus status, UUID pendingBundleId,
        BigDecimal subscriptionPrice, PartnerSubscription.ShareType partnerShareType, BigDecimal partnerShareValue
) {
    public static SubscriptionResponse from(PartnerSubscription s) {
        return new SubscriptionResponse(s.getId(), s.getPartner().getId(), s.getBundleId(),
                s.getBundleVersionAtSubscription(), s.getStatus(), s.getPendingBundleId(),
                s.getSubscriptionPrice(), s.getPartnerShareType(), s.getPartnerShareValue());
    }
}
