package com.company.partnership.ecosystembundle.service;

import com.company.partnership.ecosystembundle.domain.Bundle;
import com.company.partnership.ecosystembundle.domain.EcoSystem;
import com.company.partnership.ecosystembundle.dto.BundleRequest;
import com.company.partnership.ecosystembundle.exception.InvalidBundleOperationException;
import com.company.partnership.ecosystembundle.exception.NotFoundException;
import com.company.partnership.ecosystembundle.repository.BundleRepository;
import com.company.partnership.ecosystembundle.repository.EcoSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Encodes the BRD/SDD core business rules for bundles:
 *  - FR-BUN-01: create a bundle by mix-and-matching offerings within an eco-system.
 *  - FR-BUN-02: the same offering may appear in multiple bundles / eco-systems (enforced
 *    by NOT having a uniqueness constraint on offeringId across bundles \u2013 intentional).
 *  - FR-BUN-03: once PUBLISHED, a bundle's offering composition is locked.
 *  - FR-BUN-04: changing a published bundle creates a NEW version and should trigger
 *    the partner re-consent workflow (owned by partner-subscription-service; this
 *    service emits/records the supersession so that workflow can react to it \u2013 see
 *    TODO below for the EventBridge integration stub).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BundleService {

    private final BundleRepository bundleRepository;
    private final EcoSystemRepository ecoSystemRepository;

    public Bundle create(BundleRequest req) {
        EcoSystem ecoSystem = ecoSystemRepository.findById(req.ecoSystemId())
                .orElseThrow(() -> new NotFoundException("EcoSystem not found: " + req.ecoSystemId()));

        Bundle bundle = new Bundle();
        bundle.setEcoSystem(ecoSystem);
        bundle.setName(req.name());
        bundle.setOfferingIds(req.offeringIds());
        bundle.setVersion(1);
        bundle.setStatus(Bundle.BundleStatus.DRAFT);
        return bundleRepository.save(bundle);
    }

    public Bundle publish(UUID bundleId) {
        Bundle bundle = findById(bundleId);
        if (bundle.getStatus() != Bundle.BundleStatus.DRAFT) {
            throw new InvalidBundleOperationException(
                    "Only DRAFT bundles can be published; bundle " + bundleId + " is " + bundle.getStatus());
        }
        bundle.setStatus(Bundle.BundleStatus.PUBLISHED);
        bundle.setPublishedAt(Instant.now());
        return bundle;
    }

    /**
     * Creates a new version of a published bundle with a revised offering mix.
     * The prior version is marked SUPERSEDED. Does NOT mutate the original bundle's
     * offeringIds (FR-BUN-03). Callers (or an async listener on the emitted event)
     * are responsible for triggering partner re-consent in partner-subscription-service.
     *
     * TODO (integration stub, see /api-contracts): publish a BundleSuperseded domain
     * event to EventBridge so partner-subscription-service can flag affected
     * PartnerSubscriptions as PENDING_RECONSENT without a synchronous call.
     */
    public Bundle createNewVersion(UUID existingBundleId, BundleRequest req) {
        Bundle existing = findById(existingBundleId);
        if (existing.getStatus() != Bundle.BundleStatus.PUBLISHED) {
            throw new InvalidBundleOperationException(
                    "Can only version a PUBLISHED bundle; bundle " + existingBundleId + " is " + existing.getStatus());
        }

        Bundle newVersion = new Bundle();
        newVersion.setEcoSystem(existing.getEcoSystem());
        newVersion.setName(req.name());
        newVersion.setOfferingIds(req.offeringIds());
        newVersion.setVersion(existing.getVersion() + 1);
        newVersion.setSupersedesBundleId(existing.getId());
        newVersion.setStatus(Bundle.BundleStatus.DRAFT);

        existing.setStatus(Bundle.BundleStatus.SUPERSEDED);
        existing.setUpdatedAt(Instant.now());

        return bundleRepository.save(newVersion);
    }

    @Transactional(readOnly = true)
    public Bundle findById(UUID id) {
        return bundleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bundle not found: " + id));
    }

    @Transactional(readOnly = true)
    public java.util.List<Bundle> findByEcoSystem(UUID ecoSystemId) {
        return bundleRepository.findByEcoSystemId(ecoSystemId);
    }
}
