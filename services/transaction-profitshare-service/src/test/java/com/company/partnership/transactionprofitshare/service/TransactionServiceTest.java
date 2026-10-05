package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.client.CatalogClient;
import com.company.partnership.transactionprofitshare.client.CatalogClient.BundleInfo;
import com.company.partnership.transactionprofitshare.client.CatalogClient.OfferingInfo;
import com.company.partnership.transactionprofitshare.client.CatalogClient.SubscriptionTerms;
import com.company.partnership.transactionprofitshare.domain.OfferingLine;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.TransactionRequest;
import com.company.partnership.transactionprofitshare.exception.UnprocessableTransactionException;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CatalogClient catalog;
    private TransactionService service;

    private final UUID subId = UUID.randomUUID(), partnerId = UUID.randomUUID(), bundleId = UUID.randomUUID(), ecoId = UUID.randomUUID();
    private final UUID simId = UUID.randomUUID(), healthId = UUID.randomUUID(), simVendor = UUID.randomUUID(), insurer = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepository, catalog);
    }

    private void givenTerms(String status, String price, String shareType, String shareValue) {
        when(catalog.getSubscription(subId)).thenReturn(new SubscriptionTerms(subId, partnerId, bundleId, 1, status,
                new BigDecimal(price), shareType, new BigDecimal(shareValue)));
    }

    private void givenBundle() {
        when(catalog.getBundle(bundleId)).thenReturn(new BundleInfo(bundleId, ecoId, "Student Plus", 1, List.of(simId, healthId)));
        when(catalog.getOffering(simId)).thenReturn(new OfferingInfo(simId, simVendor, "Student SIM", "FIXED", new BigDecimal("120")));
        when(catalog.getOffering(healthId)).thenReturn(new OfferingInfo(healthId, insurer, "Student Health Cover", "PERCENTAGE", new BigDecimal("20")));
    }

    private TransactionRequest request() {
        return new TransactionRequest("  Aisyah Rahman ", "+60 12-345 6789", "aisyah@example.com", subId, null, null, null);
    }

    @Test
    void snapshotsTheBundleOfferingsAndBreakdownAtPurchase() {
        givenTerms("ACTIVE", "2000.00", "PERCENTAGE", "15");
        givenBundle();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        service.record(request());

        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(saved.capture());
        Transaction tx = saved.getValue();
        assertThat(tx.getCustomerName()).isEqualTo("Aisyah Rahman");
        assertThat(tx.getSubscriptionId()).isEqualTo(subId);
        assertThat(tx.getPartnerId()).isEqualTo(partnerId);
        assertThat(tx.getBundleName()).isEqualTo("Student Plus");
        assertThat(tx.getAmount()).isEqualByComparingTo("2000.00");
        assertThat(tx.getOfferingLines()).extracting(OfferingLine::getOfferingName)
                .containsExactly("Student SIM", "Student Health Cover");
        assertThat(tx.getOfferingLines()).extracting(l -> l.getAmount().toPlainString())
                .containsExactly("120.00", "400.00");
        assertThat(tx.getPartnerAmount()).isEqualByComparingTo("300.00");
        assertThat(tx.getCompanyAmount()).isEqualByComparingTo("1180.00");
    }

    @Test
    void stillSellsWhileTheProgrammeAwaitsReconsent() {
        givenTerms("PENDING_RECONSENT", "2000.00", "FIXED", "100");
        givenBundle();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.record(request()).getBundleId()).isEqualTo(bundleId);
    }

    @Test
    void refusesACancelledProgramme() {
        givenTerms("CANCELLED", "2000.00", "FIXED", "100");
        assertThatThrownBy(() -> service.record(request())).isInstanceOf(UnprocessableTransactionException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void refusesTermsThatDontAddUp() {
        givenTerms("ACTIVE", "300.00", "PERCENTAGE", "50");
        givenBundle();
        assertThatThrownBy(() -> service.record(request())).isInstanceOf(UnprocessableTransactionException.class);
        verify(transactionRepository, never()).save(any());
    }
}
