package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.OfferingLine;
import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.ProfitShareReportResponse;
import com.company.partnership.transactionprofitshare.exception.NotFoundException;
import com.company.partnership.transactionprofitshare.repository.ProfitShareReportRepository;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * MVP profit-share reporting: PERIODIC and REPORTING-ONLY (BRD Section 6.1). Intended to be
 * triggered by a scheduled job (TDD Section 4.4) rather than per transaction.
 *
 * Nothing is re-derived here: each transaction already carries its own breakdown, fixed at
 * purchase from the terms agreed for the partner's programme. A run sums those snapshots
 * for the period and marks the transactions as reported, in one database transaction, so
 * re-running a period can't count a sale twice.
 *
 * No payment/money movement happens here – export (CSV/PDF to S3, TDD 4.3) is not yet built.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProfitShareCalculationService {

    private final TransactionRepository transactionRepository;
    private final ProfitShareReportRepository reportRepository;

    /** Returns null when no unreported transactions fall inside the period. */
    public ProfitShareReportResponse runPeriodicCalculation(LocalDate periodStart, LocalDate periodEnd) {
        Instant from = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant cutoff = periodEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Transaction> pending = transactionRepository.findReportable(from, cutoff);
        if (pending.isEmpty()) {
            return null;
        }

        BigDecimal total = BigDecimal.ZERO, vendors = BigDecimal.ZERO, partners = BigDecimal.ZERO, company = BigDecimal.ZERO;
        for (Transaction tx : pending) {
            total = total.add(tx.getAmount());
            vendors = vendors.add(tx.getOfferingLines().stream().map(OfferingLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            partners = partners.add(tx.getPartnerAmount());
            company = company.add(tx.getCompanyAmount());
        }

        ProfitShareReport report = new ProfitShareReport();
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setStatus(ProfitShareReport.ReportStatus.GENERATED);
        report.setTransactionCount(pending.size());
        report.setTotalAmount(total);
        report.setVendorTotal(vendors);
        report.setPartnerTotal(partners);
        report.setCompanyTotal(company);
        ProfitShareReport saved = reportRepository.save(report);
        pending.forEach(tx -> tx.setIncludedInReportId(saved.getId()));
        return respond(saved);
    }

    @Transactional(readOnly = true)
    public ProfitShareReportResponse findReport(UUID id) {
        return respond(reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Report not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<ProfitShareReportResponse> findReports() {
        return reportRepository.findAll().stream()
                .sorted((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()))
                .map(this::respond).toList();
    }

    private ProfitShareReportResponse respond(ProfitShareReport r) {
        return ProfitShareReportResponse.from(r, transactionRepository.vendorTotalsForReport(r.getId()));
    }
}
