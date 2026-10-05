package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A customer (e.g. a student) subscribing, through a partner, to that partner's WHOLE
 * Bundle. Customers never pick a single offering, so a transaction carries no offering
 * or vendor. Basis for periodic profit-share computation (BRD Section 6).
 *
 * Customer contact details are personal data held by this service \u2013 relevant to the
 * data-residency work (see docs/TDD-summary.md). Insurance policy-level fields
 * (premium, sum insured, policy number) are optional, BRD Section 6.2.
 */
@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    /** The customer (e.g. student) who subscribed, registered directly at transaction time. */
    @Column(nullable = false, length = 200)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String customerPhone;

    @Column(nullable = false, length = 254)
    private String customerEmail;

    /** References ecosystem-bundle-service Bundle.id \u2013 the bundle this transaction was made under. */
    @Column(nullable = false)
    private UUID bundleId;

    /** References ecosystem-bundle-service EcoSystem.id, denormalised for profit-share rule fallback. */
    @Column(nullable = false)
    private UUID ecoSystemId;

    /** References partner-subscription-service Partner.id, denormalised here for profit-share attribution. */
    @Column(nullable = false)
    private UUID partnerId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // --- Insurance policy-level data (BRD Section 6.2); optional, only when the bundle includes insurance ---
    @Column(precision = 14, scale = 2)
    private BigDecimal premium;

    @Column(precision = 14, scale = 2)
    private BigDecimal sumInsured;

    private String policyNumber;

    @Column(nullable = false)
    private Instant transactionTimestamp = Instant.now();

    /** Set once this transaction has been included in a ProfitShareReport batch run. */
    private UUID includedInReportId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
