package com.company.partnership.transactionprofitshare.repository;

import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitShareReportRepository extends JpaRepository<ProfitShareReport, java.util.UUID> {
}
