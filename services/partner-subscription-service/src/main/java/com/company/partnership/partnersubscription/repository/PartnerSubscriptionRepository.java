package com.company.partnership.partnersubscription.repository;

import com.company.partnership.partnersubscription.domain.PartnerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PartnerSubscriptionRepository extends JpaRepository<PartnerSubscription, UUID> {
    List<PartnerSubscription> findByPartnerId(UUID partnerId);
    List<PartnerSubscription> findByBundleIdAndStatus(UUID bundleId, PartnerSubscription.SubscriptionStatus status);
}
