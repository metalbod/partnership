package com.company.partnership.transactionprofitshare.repository;

import com.company.partnership.transactionprofitshare.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, java.util.UUID> {
    List<Transaction> findByIncludedInReportIdIsNullAndTransactionTimestampBefore(Instant cutoff);
}
