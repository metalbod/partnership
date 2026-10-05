package com.company.partnership.transactionprofitshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Base URLs of the three services this one reads commercial terms from when a
 * transaction is recorded (see client/RemoteCatalogClient).
 */
@ConfigurationProperties(prefix = "app.upstream")
public record UpstreamProperties(String vendorOfferingUrl, String ecosystemBundleUrl, String partnerSubscriptionUrl) {
}
