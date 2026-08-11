package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.TransactionRequest;
import com.company.partnership.transactionprofitshare.exception.NotFoundException;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// FR-RPT-01, FR-RPT-02: capture a transaction, incl. insurance policy-level data.
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction record(TransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setConsumerEnrolmentId(req.consumerEnrolmentId());
        tx.setOfferingId(req.offeringId());
        tx.setBundleId(req.bundleId());
        tx.setPartnerId(req.partnerId());
        tx.setVendorId(req.vendorId());
        tx.setAmount(req.amount());
        tx.setInsuranceOffering(req.isInsuranceOffering());
        if (req.isInsuranceOffering()) {
            tx.setPremium(req.premium());
            tx.setSumInsured(req.sumInsured());
            tx.setPolicyNumber(req.policyNumber());
        }
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }
}
