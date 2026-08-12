package com.company.partnership.ecosystembundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Eco-System & Bundle Service \u2013 Partnership Pillar Platform.
 * See /docs and CLAUDE.md at the workspace root for domain model and business rules.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EcosystembundleApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcosystembundleApplication.class, args);
    }
}
