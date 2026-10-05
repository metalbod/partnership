package com.company.partnership.transactionprofitshare.dto;

import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository.VendorTotal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProfitShareReportResponse(
        UUID id, LocalDate periodStart, LocalDate periodEnd, Instant generatedAt,
        ProfitShareReport.ReportStatus status, String exportFileReference, int transactionCount,
        BigDecimal totalAmount, BigDecimal vendorTotal, BigDecimal partnerTotal, BigDecimal companyTotal,
        List<VendorLine> vendors
) {
    public record VendorLine(UUID vendorId, BigDecimal amount) {
    }

    public static ProfitShareReportResponse from(ProfitShareReport r, List<VendorTotal> vendors) {
        return new ProfitShareReportResponse(r.getId(), r.getPeriodStart(), r.getPeriodEnd(), r.getGeneratedAt(),
                r.getStatus(), r.getExportFileReference(), r.getTransactionCount(),
                r.getTotalAmount(), r.getVendorTotal(), r.getPartnerTotal(), r.getCompanyTotal(),
                vendors.stream().map(v -> new VendorLine(v.getVendorId(), v.getAmount())).toList());
    }
}
