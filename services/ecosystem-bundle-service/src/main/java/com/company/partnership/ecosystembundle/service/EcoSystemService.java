package com.company.partnership.ecosystembundle.service;

import com.company.partnership.ecosystembundle.domain.EcoSystem;
import com.company.partnership.ecosystembundle.dto.EcoSystemRequest;
import com.company.partnership.ecosystembundle.exception.NotFoundException;
import com.company.partnership.ecosystembundle.repository.EcoSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// FR-ECO-01, FR-ECO-02: create eco-systems; may hold assigned offerings with no bundles yet.
@Service
@RequiredArgsConstructor
@Transactional
public class EcoSystemService {

    private final EcoSystemRepository ecoSystemRepository;

    public EcoSystem create(EcoSystemRequest req) {
        EcoSystem ecoSystem = new EcoSystem();
        ecoSystem.setName(req.name());
        ecoSystem.setTheme(req.theme());
        return ecoSystemRepository.save(ecoSystem);
    }

    public EcoSystem assignOffering(UUID ecoSystemId, UUID offeringId) {
        EcoSystem ecoSystem = findById(ecoSystemId);
        if (!ecoSystem.getAssignedOfferingIds().contains(offeringId)) {
            ecoSystem.getAssignedOfferingIds().add(offeringId);
        }
        return ecoSystem;
    }

    @Transactional(readOnly = true)
    public List<EcoSystem> findAll() {
        return ecoSystemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EcoSystem findById(UUID id) {
        return ecoSystemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("EcoSystem not found: " + id));
    }
}
