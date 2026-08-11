package com.company.partnership.partnersubscription.service;

import com.company.partnership.partnersubscription.domain.Partner;
import com.company.partnership.partnersubscription.domain.PartnerSubscription;
import com.company.partnership.partnersubscription.dto.SubscribeRequest;
import com.company.partnership.partnersubscription.exception.InvalidSubscriptionOperationException;
import com.company.partnership.partnersubscription.exception.NotFoundException;
import com.company.partnership.partnersubscription.repository.PartnerRepository;
import com.company.partnership.partnersubscription.repository.PartnerSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FR-PTR-02/03: whole-bundle-only subscription, across any number of eco-systems.
 * FR-BUN-04 / FR-PTR-07: re-consent workflow when a subscribed bundle is superseded.
 *
 * TODO (integration stub): this service should consume a BundleSuperseded event
 * from ecosystem-bundle-service (via EventBridge \u2013 see /api-contracts) and call
 * flagPendingReconsent() for every affected subscription, rather than relying on
 * a synchronous caller. A REST endpoint is also provided below for MVP simplicity
 * ahead of the event listener being wired up.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final PartnerSubscriptionRepository subscriptionRepository;
    private final PartnerRepository partnerRepository;

    public PartnerSubscription subscribe(SubscribeRequest req) {
        Partner partner = partnerRepository.findById(req.partnerId())
                .orElseThrow(() -> new NotFoundException("Partner not found: " + req.partnerId()));

        PartnerSubscription subscription = new PartnerSubscription();
        subscription.setPartner(partner);
        subscription.setBundleId(req.bundleId());
        subscription.setBundleVersionAtSubscription(req.bundleVersion());
        subscription.setStatus(PartnerSubscription.SubscriptionStatus.ACTIVE);
        subscription.setConsentedAt(Instant.now());
        return subscriptionRepository.save(subscription);
    }

    /** Called (for now, synchronously) when ecosystem-bundle-service supersedes a bundle. */
    public void flagPendingReconsent(UUID oldBundleId, UUID newBundleId) {
        List<PartnerSubscription> affected = subscriptionRepository
                .findByBundleIdAndStatus(oldBundleId, PartnerSubscription.SubscriptionStatus.ACTIVE);
        for (PartnerSubscription sub : affected) {
            sub.setStatus(PartnerSubscription.SubscriptionStatus.PENDING_RECONSENT);
            sub.setPendingBundleId(newBundleId);
            // Consumers on `sub` continue against the ORIGINAL bundleId/version until re-consent
            // (BRD Section 4.2) \u2013 do not change bundleId here.
        }
    }

    public PartnerSubscription reconsent(UUID subscriptionId, int newBundleVersion) {
        PartnerSubscription sub = findById(subscriptionId);
        if (sub.getStatus() != PartnerSubscription.SubscriptionStatus.PENDING_RECONSENT) {
            throw new InvalidSubscriptionOperationException(
                    "Subscription " + subscriptionId + " is not pending re-consent (status=" + sub.getStatus() + ")");
        }
        sub.setBundleId(sub.getPendingBundleId());
        sub.setBundleVersionAtSubscription(newBundleVersion);
        sub.setPendingBundleId(null);
        sub.setStatus(PartnerSubscription.SubscriptionStatus.ACTIVE);
        sub.setReconsentedAt(Instant.now());
        return sub;
    }

    @Transactional(readOnly = true)
    public List<PartnerSubscription> findByPartner(UUID partnerId) {
        return subscriptionRepository.findByPartnerId(partnerId);
    }

    @Transactional(readOnly = true)
    public PartnerSubscription findById(UUID id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + id));
    }
}
