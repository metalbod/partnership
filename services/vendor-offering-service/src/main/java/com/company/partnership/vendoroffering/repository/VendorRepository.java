package com.company.partnership.vendoroffering.repository;

import com.company.partnership.vendoroffering.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
}
