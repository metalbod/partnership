package com.company.partnership.vendoroffering.repository;

import com.company.partnership.vendoroffering.domain.Offering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {

    // Supports FR-VEN-04: offerings may exist without eco-system/bundle assignment;
    // that association lives in ecosystem-bundle-service, not here.
    List<Offering> findByVendorId(UUID vendorId);
}
