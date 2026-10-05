package com.company.partnership.transactionprofitshare.dto;

import com.company.partnership.transactionprofitshare.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id, String customerName, String customerPhone, String customerEmail,
        UUID partnerId, UUID bundleId, UUID ecoSystemId, BigDecimal amount,
        BigDecimal premium, BigDecimal sumInsured, String policyNumber, Instant transactionTimestamp
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getCustomerName(), t.getCustomerPhone(), t.getCustomerEmail(),
                t.getPartnerId(), t.getBundleId(), t.getEcoSystemId(), t.getAmount(),
                t.getPremium(), t.getSumInsured(), t.getPolicyNumber(), t.getTransactionTimestamp());
    }
}
