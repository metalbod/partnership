package com.company.partnership.transactionprofitshare.repository;

import com.company.partnership.transactionprofitshare.domain.ProfitShareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfitShareRuleRepository extends JpaRepository<ProfitShareRule, UUID> {

    /** Tier 1 (most specific): an exact vendor+bundle+partner match. */
    Optional<ProfitShareRule> findFirstByVendorIdAndBundleIdAndPartnerId(UUID vendorId, UUID bundleId, UUID partnerId);

    /** Tier 2: applies to any vendor/partner under a given bundle. */
    Optional<ProfitShareRule> findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(UUID bundleId);

    /** Tier 3 (least specific): applies to any bundle/vendor/partner under a given eco-system. */
    Optional<ProfitShareRule> findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(UUID ecoSystemId);
}
