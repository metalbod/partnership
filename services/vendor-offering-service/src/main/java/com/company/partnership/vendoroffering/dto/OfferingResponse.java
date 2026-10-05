package com.company.partnership.vendoroffering.dto;

import com.company.partnership.vendoroffering.domain.Offering;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferingResponse(
        UUID id,
        UUID vendorId,
        String name,
        String description,
        Offering.OfferingType offeringType,
        Offering.PriceType priceType,
        BigDecimal priceValue,
        Offering.OfferingStatus status
) {
    public static OfferingResponse from(Offering o) {
        return new OfferingResponse(
                o.getId(), o.getVendor().getId(), o.getName(), o.getDescription(),
                o.getOfferingType(), o.getPriceType(), o.getPriceValue(), o.getStatus());
    }
}
