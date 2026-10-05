package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.TransactionRequest;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void recordsCustomerDetailsAgainstPartnerAndWholeBundle() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        TransactionService service = new TransactionService(transactionRepository);

        UUID partnerId = UUID.randomUUID(), bundleId = UUID.randomUUID(), ecoSystemId = UUID.randomUUID();
        service.record(new TransactionRequest(
                "  Aisyah Rahman ", "+60 12-345 6789", "aisyah@example.com",
                partnerId, bundleId, ecoSystemId, new BigDecimal("480.00"), null, null, null));

        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(saved.capture());
        Transaction tx = saved.getValue();
        assertThat(tx.getCustomerName()).isEqualTo("Aisyah Rahman");
        assertThat(tx.getCustomerPhone()).isEqualTo("+60 12-345 6789");
        assertThat(tx.getCustomerEmail()).isEqualTo("aisyah@example.com");
        assertThat(tx.getPartnerId()).isEqualTo(partnerId);
        assertThat(tx.getBundleId()).isEqualTo(bundleId);
        assertThat(tx.getEcoSystemId()).isEqualTo(ecoSystemId);
        assertThat(tx.getPolicyNumber()).isNull();
    }
}
