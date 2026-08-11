package com.company.partnership.partnersubscription.repository;

import com.company.partnership.partnersubscription.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartnerRepository extends JpaRepository<Partner, UUID> {
}
