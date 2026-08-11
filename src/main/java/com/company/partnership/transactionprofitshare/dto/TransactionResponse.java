package com.company.partnership.transactionprofitshare.dto;

import com.company.partnership.transactionprofitshare.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id, UUID offeringId, UUID bundleId, UUID partnerId, UUID vendorId,
        BigDecimal amount, boolean isInsuranceOffering, Instant transactionTimestamp
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getOfferingId(), t.getBundleId(), t.getPartnerId(),
                t.getVendorId(), t.getAmount(), t.isInsuranceOffering(), t.getTransactionTimestamp());
    }
}
