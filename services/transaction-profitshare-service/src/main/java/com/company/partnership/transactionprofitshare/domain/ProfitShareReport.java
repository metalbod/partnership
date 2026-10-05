package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A generated periodic (e.g. monthly) transaction listing + profit-share summary
 * report. MVP: reporting-only, no payment execution (BRD Section 6.1).
 */
@Entity
@Table(name = "profit_share_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfitShareReport {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    private Instant generatedAt = Instant.now();

    private int transactionCount;

    /** Totals across the transactions in this report, from each transaction's own snapshot. */
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal vendorTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal partnerTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal companyTotal = BigDecimal.ZERO;

    /** S3 object key/URI of the exported report file (see TDD Section 4.3). */
    private String exportFileReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.GENERATED;

    public enum ReportStatus {
        GENERATED, EXPORTED
    }
}
