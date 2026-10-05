package com.company.partnership.partnersubscription.dto;

import com.company.partnership.partnersubscription.domain.PartnerSubscription;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * FR-PTR-02: subscribe a partner to a WHOLE bundle version \u2013 no offering-level selection
 * field exists by design. Also fixes the partner's commercial terms: the cost of the
 * bundle (subscriptionPrice) and the partner's share of it, as a FIXED MYR amount or a
 * PERCENTAGE of that cost.
 */
public record SubscribeRequest(
        @NotNull UUID partnerId,
        @NotNull UUID bundleId,
        @NotNull Integer bundleVersion,
        @NotNull @Positive BigDecimal subscriptionPrice,
        @NotNull PartnerSubscription.ShareType partnerShareType,
        @NotNull @PositiveOrZero BigDecimal partnerShareValue
) {
    @AssertTrue(message = "The partner's share can't exceed 100% or the subscription price")
    public boolean isPartnerShareWithinLimit() {
        if (partnerShareType == null || partnerShareValue == null || subscriptionPrice == null) return true;
        BigDecimal limit = partnerShareType == PartnerSubscription.ShareType.PERCENTAGE
                ? BigDecimal.valueOf(100) : subscriptionPrice;
        return partnerShareValue.compareTo(limit) <= 0;
    }
}
