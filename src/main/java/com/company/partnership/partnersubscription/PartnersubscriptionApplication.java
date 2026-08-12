package com.company.partnership.partnersubscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Partner & Subscription Service \u2013 Partnership Pillar Platform.
 * See /docs and CLAUDE.md at the workspace root for domain model and business rules.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PartnersubscriptionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnersubscriptionApplication.class, args);
    }
}
