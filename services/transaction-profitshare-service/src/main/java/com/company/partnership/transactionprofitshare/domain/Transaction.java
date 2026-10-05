package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A customer (e.g. a student) subscribing, through a partner, to that partner's WHOLE
 * Bundle. Customers never pick a single offering, so a transaction carries no offering
 * or vendor. Basis for periodic profit-share computation (BRD Section 6).
 *
 * The sale is priced and split at the moment it is recorded (offering lines, partner and
 * company amounts) from the terms agreed for the partner's programme, and that snapshot
 * is what profit-share reports aggregate \u2013 never today's terms.
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

    /** The partner programme (PartnerSubscription) this sale was made under. Null on legacy rows. */
    private UUID subscriptionId;

    /** References ecosystem-bundle-service Bundle.id \u2013 the bundle version the customer took. */
    @Column(nullable = false)
    private UUID bundleId;

    /** Snapshot of the bundle's name/version at purchase. Null on legacy rows. */
    private String bundleName;

    private Integer bundleVersion;

    /** References ecosystem-bundle-service EcoSystem.id, denormalised for reporting. */
    @Column(nullable = false)
    private UUID ecoSystemId;

    /** References partner-subscription-service Partner.id, denormalised here for profit-share attribution. */
    @Column(nullable = false)
    private UUID partnerId;

    /** What the customer paid: the bundle cost agreed for this partner's programme. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /**
     * The offerings that were in the bundle at the moment of purchase, each with the vendor's
     * price terms and the amount it earned. EAGER: always serialised, open-in-view is off.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transaction_offering_line", joinColumns = @JoinColumn(name = "transaction_id"))
    private List<OfferingLine> offeringLines = new ArrayList<>();

    /** The partner's share of this sale, per the programme's terms at purchase. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal partnerAmount = BigDecimal.ZERO;

    /** The company's share: the remainder after vendors and partner. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal companyAmount = BigDecimal.ZERO;

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
