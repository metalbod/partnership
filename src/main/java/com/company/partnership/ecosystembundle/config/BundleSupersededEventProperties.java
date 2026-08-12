package com.company.partnership.ecosystembundle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.events.bundle-superseded")
public record BundleSupersededEventProperties(String eventBusName, String source, String detailType) {
}
