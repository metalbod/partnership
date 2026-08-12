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
 * A record of a consumer using/redeeming/purchasing an Offering under a subscribed
 * Bundle. Basis for periodic profit-share computation (BRD Section 6).
 *
 * For insurance offerings, policy-level fields are captured per BRD Section 6.2
 * ("Data Captured for Profit-Share") \u2013 premium, sum insured, policy number.
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

    /** References a consumer enrolment record (external ID from Store-Front pillar via Consumer Enrolment API). */
    @Column(nullable = false)
    private UUID consumerEnrolmentId;

    /** References vendor-offering-service Offering.id. */
    @Column(nullable = false)
    private UUID offeringId;

    /** References ecosystem-bundle-service Bundle.id \u2013 the bundle this transaction was made under. */
    @Column(nullable = false)
    private UUID bundleId;

    /** References ecosystem-bundle-service EcoSystem.id, denormalised for profit-share rule fallback. */
    @Column(nullable = false)
    private UUID ecoSystemId;

    /** References partner-subscription-service Partner.id, denormalised here for profit-share attribution. */
    @Column(nullable = false)
    private UUID partnerId;

    /** References vendor-offering-service Vendor.id, denormalised here for profit-share attribution. */
    @Column(nullable = false)
    private UUID vendorId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    private boolean isInsuranceOffering = false;

    // --- Insurance policy-level data (BRD Section 6.2); null for non-insurance offerings ---
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
