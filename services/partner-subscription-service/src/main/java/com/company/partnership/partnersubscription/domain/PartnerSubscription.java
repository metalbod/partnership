package com.company.partnership.partnersubscription.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    /** Set when a new bundle version supersedes the one this subscription points to. */
    private UUID pendingBundleId;

    private Instant consentedAt = Instant.now();
    private Instant reconsentedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum SubscriptionStatus {
        ACTIVE, PENDING_RECONSENT, SUPERSEDED, CANCELLED
    }
}
