package com.company.partnership.vendoroffering.service;

import com.company.partnership.vendoroffering.domain.Vendor;
import com.company.partnership.vendoroffering.dto.VendorRequest;
import com.company.partnership.vendoroffering.exception.NotFoundException;
import com.company.partnership.vendoroffering.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// FR-VEN-01, FR-VEN-03: onboard, edit, deactivate/archive a vendor.
@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    public Vendor create(VendorRequest req) {
        Vendor vendor = new Vendor();
        vendor.setName(req.name());
        vendor.setContactEmail(req.contactEmail());
        vendor.setContactPhone(req.contactPhone());
        vendor.setCommercialTerms(req.commercialTerms());
        return vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public List<Vendor> findAll() {
        return vendorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vendor findById(UUID id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found: " + id));
    }

    public Vendor update(UUID id, VendorRequest req) {
        Vendor vendor = findById(id);
        vendor.setName(req.name());
        vendor.setContactEmail(req.contactEmail());
        vendor.setContactPhone(req.contactPhone());
        vendor.setCommercialTerms(req.commercialTerms());
        vendor.setUpdatedAt(Instant.now());
        return vendor;
    }

    public void deactivate(UUID id) {
        Vendor vendor = findById(id);
        vendor.setStatus(Vendor.VendorStatus.INACTIVE);
        vendor.setUpdatedAt(Instant.now());
    }
}
