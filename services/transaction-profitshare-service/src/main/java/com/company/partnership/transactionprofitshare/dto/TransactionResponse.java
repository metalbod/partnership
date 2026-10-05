package com.company.partnership.transactionprofitshare.dto;

import com.company.partnership.transactionprofitshare.domain.OfferingLine;
import com.company.partnership.transactionprofitshare.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(
        UUID id, String customerName, String customerPhone, String customerEmail,
        UUID subscriptionId, UUID partnerId, UUID bundleId, String bundleName, Integer bundleVersion, UUID ecoSystemId,
        BigDecimal amount, List<Line> offerings, BigDecimal vendorTotal, BigDecimal partnerAmount, BigDecimal companyAmount,
        BigDecimal premium, BigDecimal sumInsured, String policyNumber,
        Instant transactionTimestamp, UUID includedInReportId
) {
    public record Line(UUID offeringId, String offeringName, UUID vendorId,
                       OfferingLine.PriceType priceType, BigDecimal priceValue, BigDecimal amount) {
    }

    public static TransactionResponse from(Transaction t) {
        List<Line> lines = t.getOfferingLines().stream()
                .map(l -> new Line(l.getOfferingId(), l.getOfferingName(), l.getVendorId(), l.getPriceType(), l.getPriceValue(), l.getAmount()))
                .toList();
        BigDecimal vendorTotal = lines.stream().map(Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TransactionResponse(t.getId(), t.getCustomerName(), t.getCustomerPhone(), t.getCustomerEmail(),
                t.getSubscriptionId(), t.getPartnerId(), t.getBundleId(), t.getBundleName(), t.getBundleVersion(), t.getEcoSystemId(),
                t.getAmount(), lines, vendorTotal, t.getPartnerAmount(), t.getCompanyAmount(),
                t.getPremium(), t.getSumInsured(), t.getPolicyNumber(),
                t.getTransactionTimestamp(), t.getIncludedInReportId());
    }
}
