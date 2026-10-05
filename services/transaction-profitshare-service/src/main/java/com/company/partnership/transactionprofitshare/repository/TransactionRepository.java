package com.company.partnership.transactionprofitshare.repository;

import com.company.partnership.transactionprofitshare.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Unreported transactions in [from, cutoff) that carry a purchase-time snapshot. Legacy rows
     * recorded before snapshots existed (no subscription) are skipped: they have no breakdown, so
     * including them would make a report's totals not add up.
     */
    @Query("select t from Transaction t where t.includedInReportId is null and t.subscriptionId is not null "
            + "and t.transactionTimestamp >= :from and t.transactionTimestamp < :cutoff")
    List<Transaction> findReportable(@Param("from") Instant from, @Param("cutoff") Instant cutoff);

    List<Transaction> findAllByOrderByTransactionTimestampDesc();

    interface VendorTotal {
        UUID getVendorId();

        BigDecimal getAmount();
    }

    /** What each vendor earned across the transactions that went into one report. */
    @Query("select l.vendorId as vendorId, sum(l.amount) as amount "
            + "from Transaction t join t.offeringLines l "
            + "where t.includedInReportId = :reportId group by l.vendorId order by sum(l.amount) desc")
    List<VendorTotal> vendorTotalsForReport(@Param("reportId") UUID reportId);
}
