package com.company.partnership.transactionprofitshare.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * What transaction-profitshare-service needs to know about the rest of the platform at
 * the moment a transaction is recorded. Cross-service references stay UUID-only; this
 * reads the current terms over each service's public API and the result is snapshotted
 * onto the transaction, so later changes never rewrite history.
 */
public interface CatalogClient {

    SubscriptionTerms getSubscription(UUID subscriptionId);

    BundleInfo getBundle(UUID bundleId);

    OfferingInfo getOffering(UUID offeringId);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubscriptionTerms(UUID id, UUID partnerId, UUID bundleId, int bundleVersionAtSubscription, String status,
                             BigDecimal subscriptionPrice, String partnerShareType, BigDecimal partnerShareValue) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record BundleInfo(UUID id, UUID ecoSystemId, String name, int version, List<UUID> offeringIds) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OfferingInfo(UUID id, UUID vendorId, String name, String priceType, BigDecimal priceValue) {
    }
}
