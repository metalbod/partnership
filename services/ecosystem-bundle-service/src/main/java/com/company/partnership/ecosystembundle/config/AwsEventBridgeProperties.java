package com.company.partnership.ecosystembundle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * endpointOverride is blank against real AWS (SDK resolves the regional EventBridge
 * endpoint itself) and set to the LocalStack URL for local dev (see docker-compose.yml).
 */
@ConfigurationProperties(prefix = "aws.eventbridge")
public record AwsEventBridgeProperties(String region, String endpointOverride) {
}
