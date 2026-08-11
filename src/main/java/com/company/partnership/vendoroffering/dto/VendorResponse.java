package com.company.partnership.vendoroffering.dto;

import com.company.partnership.vendoroffering.domain.Vendor;

import java.util.UUID;

public record VendorResponse(
        UUID id,
        String name,
        String contactEmail,
        String contactPhone,
        Vendor.VendorStatus status
) {
    public static VendorResponse from(Vendor v) {
        return new VendorResponse(v.getId(), v.getName(), v.getContactEmail(), v.getContactPhone(), v.getStatus());
    }
}
