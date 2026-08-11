package com.company.partnership.transactionprofitshare.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(
        @NotNull UUID consumerEnrolmentId,
        @NotNull UUID offeringId,
        @NotNull UUID bundleId,
        @NotNull UUID partnerId,
        @NotNull UUID vendorId,
        @NotNull BigDecimal amount,
        boolean isInsuranceOffering,
        BigDecimal premium,
        BigDecimal sumInsured,
        String policyNumber
) {
}
