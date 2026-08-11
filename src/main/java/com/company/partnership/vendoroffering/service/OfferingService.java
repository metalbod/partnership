package com.company.partnership.vendoroffering.service;

import com.company.partnership.vendoroffering.domain.Offering;
import com.company.partnership.vendoroffering.domain.Vendor;
import com.company.partnership.vendoroffering.dto.OfferingRequest;
import com.company.partnership.vendoroffering.exception.NotFoundException;
import com.company.partnership.vendoroffering.repository.OfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// FR-VEN-02, FR-VEN-04: create offerings under a vendor; offerings may exist
// without any eco-system/bundle assignment (that link lives in ecosystem-bundle-service).
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final VendorService vendorService;

    public Offering create(OfferingRequest req) {
        Vendor vendor = vendorService.findById(req.vendorId());
        Offering offering = new Offering();
        offering.setVendor(vendor);
        offering.setName(req.name());
        offering.setDescription(req.description());
        offering.setOfferingType(req.offeringType());
        return offeringRepository.save(offering);
    }

    @Transactional(readOnly = true)
    public List<Offering> findByVendor(UUID vendorId) {
        return offeringRepository.findByVendorId(vendorId);
    }

    @Transactional(readOnly = true)
    public Offering findById(UUID id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offering not found: " + id));
    }
}
