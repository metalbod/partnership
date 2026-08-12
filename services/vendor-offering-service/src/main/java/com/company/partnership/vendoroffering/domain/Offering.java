package com.company.partnership.vendoroffering.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferingStatus status = OfferingStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum OfferingType {
        INSURANCE, NON_INSURANCE
    }

    public enum OfferingStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }
}
