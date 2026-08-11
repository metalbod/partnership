package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import com.company.partnership.transactionprofitshare.domain.ProfitShareRule;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.repository.ProfitShareReportRepository;
import com.company.partnership.transactionprofitshare.repository.ProfitShareRuleRepository;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * MVP profit-share engine: PERIODIC, REPORTING-ONLY (BRD Section 6.1). This is
 * intended to be triggered by a scheduled job (TDD Section 4.4: AWS Lambda on an
 * EventBridge Scheduler trigger) rather than called synchronously per transaction.
 *
 * No payment/money movement happens here \u2013 this only produces a ProfitShareReport
 * whose export (CSV/PDF to S3) is handled by a separate reporting/export component
 * (see TDD Section 4.3 \u2013 not yet implemented in this MVP scaffold).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProfitShareCalculationService {

    private final TransactionRepository transactionRepository;
    private final ProfitShareRuleRepository ruleRepository;
    private final ProfitShareReportRepository reportRepository;

    public record SplitResult(BigDecimal vendorAmount, BigDecimal companyAmount, BigDecimal partnerAmount) {
    }

    /**
     * Runs the periodic batch: finds all transactions not yet included in a report,
     * with a timestamp before the cutoff, resolves the applicable ProfitShareRule for
     * each, computes the three-way split, and creates a ProfitShareReport marker.
     *
     * The actual CSV/PDF export to S3 (TDD 4.3) and marking transactions with the
     * resulting report id is left as a TODO for the reporting/export component.
     */
    public ProfitShareReport runPeriodicCalculation(LocalDate periodStart, LocalDate periodEnd) {
        Instant cutoff = periodEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Transaction> pending = transactionRepository
                .findByIncludedInReportIdIsNullAndTransactionTimestampBefore(cutoff);

        // TODO: aggregate `pending` transactions by resolveRule(...) and produce line
        // items for the export; kept minimal here so the batch entry point and rule
        // resolution logic are unambiguous starting points for implementation.
        for (Transaction tx : pending) {
            SplitResult split = calculateSplit(tx);
            // TODO: persist split as a report line item once the report line entity is added.
        }

        ProfitShareReport report = new ProfitShareReport();
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setStatus(ProfitShareReport.ReportStatus.GENERATED);
        return reportRepository.save(report);
    }

    /** Resolves the most specific matching rule: partner+bundle+vendor > bundle > eco-system. */
    public SplitResult calculateSplit(Transaction tx) {
        ProfitShareRule rule = resolveRule(tx)
                .orElseThrow(() -> new IllegalStateException(
                        "No ProfitShareRule configured for transaction " + tx.getId() +
                        " (vendor=" + tx.getVendorId() + ", bundle=" + tx.getBundleId() +
                        ", partner=" + tx.getPartnerId() + ")"));

        BigDecimal amount = tx.getAmount();
        BigDecimal vendorAmount = amount.multiply(rule.getVendorSharePct())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal companyAmount = amount.multiply(rule.getCompanySharePct())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal partnerAmount = amount.multiply(rule.getPartnerSharePct())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new SplitResult(vendorAmount, companyAmount, partnerAmount);
    }

    private Optional<ProfitShareRule> resolveRule(Transaction tx) {
        // Simplified MVP resolution: exact vendor+bundle+partner match only.
        // TODO: broaden to fall back to bundle-level, then eco-system-level rules
        // as described in the class-level Javadoc, once ProfitShareRule scope
        // querying is implemented in the repository layer.
        return ruleRepository.findAll().stream()
                .filter(r -> tx.getVendorId().equals(r.getVendorId()))
                .filter(r -> tx.getBundleId().equals(r.getBundleId()))
                .filter(r -> tx.getPartnerId().equals(r.getPartnerId()))
                .findFirst();
    }
}
