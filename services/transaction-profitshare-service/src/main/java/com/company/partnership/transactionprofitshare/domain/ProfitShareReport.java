package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /** S3 object key/URI of the exported report file (see TDD Section 4.3). */
    private String exportFileReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.GENERATED;

    public enum ReportStatus {
        GENERATED, EXPORTED
    }
}
