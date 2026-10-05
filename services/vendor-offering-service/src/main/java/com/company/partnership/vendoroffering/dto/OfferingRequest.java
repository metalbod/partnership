package com.company.partnership.vendoroffering.dto;

import com.company.partnership.vendoroffering.domain.Offering;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * priceType/priceValue is the vendor's unit price for this offering inside a bundle:
 * FIXED = a MYR amount, PERCENTAGE = a percentage (0-100) of the bundle cost.
 */
public record OfferingRequest(
        @NotNull UUID vendorId,
        @NotBlank String name,
        String description,
        @NotNull Offering.OfferingType offeringType,
        @NotNull Offering.PriceType priceType,
        @NotNull @PositiveOrZero BigDecimal priceValue
) {
    @AssertTrue(message = "A percentage price must be between 0 and 100")
    public boolean isPriceWithinRange() {
        return priceType != Offering.PriceType.PERCENTAGE || priceValue == null
                || priceValue.compareTo(BigDecimal.valueOf(100)) <= 0;
    }
}
