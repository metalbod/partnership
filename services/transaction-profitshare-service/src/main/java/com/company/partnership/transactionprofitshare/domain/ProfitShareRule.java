package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A configured three-way profit-share split (Vendor / Company / Partner) applied to
 * transactions during the periodic batch calculation. Scope narrows from eco-system
 * down to a specific vendor+bundle+partner combination; the most specific matching
 * rule applies (resolution logic lives in ProfitShareCalculationService).
 */
@Entity
@Table(name = "profit_share_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfitShareRule {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID ecoSystemId;
    private UUID bundleId;
    private UUID vendorId;
    private UUID partnerId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal vendorSharePct;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal companySharePct;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal partnerSharePct;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
