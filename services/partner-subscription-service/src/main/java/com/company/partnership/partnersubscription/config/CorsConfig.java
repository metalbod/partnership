package com.company.partnership.partnersubscription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Local dev only: lets the Admin Console SPA (Vite dev server) call this service's
 * API directly from the browser. Production target is API Gateway/ALB in front of
 * all four services (see TDD Section 4.1), which will front CORS instead of this.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
