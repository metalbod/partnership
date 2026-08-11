package com.company.partnership.ecosystembundle.repository;

import com.company.partnership.ecosystembundle.domain.EcoSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EcoSystemRepository extends JpaRepository<EcoSystem, UUID> {
}
