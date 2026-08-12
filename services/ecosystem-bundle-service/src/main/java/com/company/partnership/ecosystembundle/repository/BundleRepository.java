package com.company.partnership.ecosystembundle.repository;

import com.company.partnership.ecosystembundle.domain.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BundleRepository extends JpaRepository<Bundle, UUID> {
    List<Bundle> findByEcoSystemId(UUID ecoSystemId);
}
