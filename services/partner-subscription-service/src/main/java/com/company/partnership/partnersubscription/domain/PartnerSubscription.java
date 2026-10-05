package com.company.partnership.partnersubscription.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Links a Partner to a specific Bundle VERSION. BRD Section 5.1:
 *  - A partner subscribes to a WHOLE bundle only \u2013 no partial/offering-level subscription.
 *  - When the subscribed bundle is superseded by a new version (ecosystem-bundle-service),
 *    this subscription is flagged PENDING_RECONSENT until the partner re-consents (FR-BUN-04,
 *    FR-PTR-07). Until re-consent, the partner's consumers continue on the prior bundle version.
 */
@Entity
@Table(name = "partner_subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSubscription {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    /** References ecosystem-bundle-service Bundle.id \u2013 no cross-service FK. */
    @Column(nullable = false)
    private UUID bundleId;

    @Column(nullable = false)
    private int bundleVersionAtSubscription;

    /**
     * Commercial terms agreed for this partner's programme: what the bundle costs the
     * partner's customers (MYR per subscription) and the partner's share of that cost.
     * The vendors' shares come from each offering's own unit price; the company's share
     * is what remains. transaction-profitshare-service snapshots all of this per sale.
     */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subscriptionPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareType partnerShareType;

    /** MYR when FIXED; 0\u2013100 when PERCENTAGE of subscriptionPrice. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal partnerShareValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    /** Set when a new bundle version supersedes the one this subscription points to. */
    private UUID pendingBundleId;

    private Instant consentedAt = Instant.now();
    private Instant reconsentedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum ShareType {
        FIXED, PERCENTAGE
    }

    public enum SubscriptionStatus {
        ACTIVE, PENDING_RECONSENT, SUPERSEDED, CANCELLED
    }
}
