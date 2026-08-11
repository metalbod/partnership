package com.company.partnership.partnersubscription.service;

import com.company.partnership.partnersubscription.domain.Partner;
import com.company.partnership.partnersubscription.dto.PartnerRequest;
import com.company.partnership.partnersubscription.exception.NotFoundException;
import com.company.partnership.partnersubscription.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// FR-PTR-01: onboard a partner. onboardedBy drives FR-SAL-01 Sales KPI reporting.
@Service
@RequiredArgsConstructor
@Transactional
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public Partner create(PartnerRequest req) {
        Partner partner = new Partner();
        partner.setName(req.name());
        partner.setContactEmail(req.contactEmail());
        partner.setContactPhone(req.contactPhone());
        partner.setCommercialTerms(req.commercialTerms());
        partner.setOnboardedBy(req.onboardedBy());
        return partnerRepository.save(partner);
    }

    @Transactional(readOnly = true)
    public List<Partner> findAll() {
        return partnerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Partner findById(UUID id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partner not found: " + id));
    }
}
