package com.company.partnership.transactionprofitshare.repository;

import com.company.partnership.transactionprofitshare.domain.ProfitShareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfitShareRuleRepository extends JpaRepository<ProfitShareRule, UUID> {

    /**
     * Tier 1 (most specific): an exact bundle+partner match. A transaction is a customer's
     * subscription to a whole bundle, so there is no single vendor to match on.
     */
    Optional<ProfitShareRule> findFirstByBundleIdAndPartnerIdAndVendorIdIsNull(UUID bundleId, UUID partnerId);

    /** Tier 2: applies to any partner under a given bundle. */
    Optional<ProfitShareRule> findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(UUID bundleId);

    /** Tier 3 (least specific): applies to any bundle/vendor/partner under a given eco-system. */
    Optional<ProfitShareRule> findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(UUID ecoSystemId);
}
