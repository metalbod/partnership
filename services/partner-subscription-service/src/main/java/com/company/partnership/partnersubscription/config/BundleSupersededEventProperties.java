package com.company.partnership.partnersubscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.events.bundle-superseded")
public record BundleSupersededEventProperties(String queueName) {
}
