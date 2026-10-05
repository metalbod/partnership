package com.company.partnership.transactionprofitshare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One offering inside the bundle at the moment of purchase, with the vendor's price terms
 * and the amount that went to its vendor. A snapshot: never updated after the sale.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingLine {

    @Column(nullable = false)
    private UUID offeringId;

    @Column(nullable = false)
    private String offeringName;

    @Column(nullable = false)
    private UUID vendorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceType priceType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal priceValue;

    /** What this vendor earns from the sale. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    public enum PriceType {
        FIXED, PERCENTAGE
    }
}
