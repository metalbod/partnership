package com.company.partnership.vendoroffering.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A single good or service made available by a Vendor. Treated generically by the
 * platform in MVP \u2013 offeringType is informational only and does NOT change flow,
 * per BRD Section 5.1 (Key Business Rules Summary).
 *
 * Insurance offerings additionally require policy-level data at the point of
 * transaction (captured in transaction-profitshare-service), not here.
 */
@Entity
@Table(name = "offering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offering {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    /** Informational tag only \u2013 insurance and non-insurance offerings follow the same flow in MVP. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferingType offeringType;

    /**
     * The vendor's unit price for this offering when it is sold inside a bundle: either a
     * FIXED amount (MYR) or a PERCENTAGE of the bundle cost. Used by
     * transaction-profitshare-service to compute the vendor's share of each transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType priceType = PriceType.FIXED;

    /** MYR when priceType is FIXED; 0\u2013100 when PERCENTAGE. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal priceValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferingStatus status = OfferingStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum OfferingType {
        INSURANCE, NON_INSURANCE
    }

    public enum PriceType {
        FIXED, PERCENTAGE
    }

    public enum OfferingStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }
}
