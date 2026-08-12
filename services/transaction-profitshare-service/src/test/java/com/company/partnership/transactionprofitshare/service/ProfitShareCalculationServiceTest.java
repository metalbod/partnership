package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.ProfitShareRule;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.repository.ProfitShareReportRepository;
import com.company.partnership.transactionprofitshare.repository.ProfitShareRuleRepository;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitShareCalculationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ProfitShareRuleRepository ruleRepository;
    @Mock
    private ProfitShareReportRepository reportRepository;

    private ProfitShareCalculationService service;

    private final UUID ecoSystemId = UUID.randomUUID();
    private final UUID bundleId = UUID.randomUUID();
    private final UUID vendorId = UUID.randomUUID();
    private final UUID partnerId = UUID.randomUUID();
    private Transaction tx;

    @BeforeEach
    void setUp() {
        service = new ProfitShareCalculationService(transactionRepository, ruleRepository, reportRepository);

        tx = new Transaction();
        tx.setEcoSystemId(ecoSystemId);
        tx.setBundleId(bundleId);
        tx.setVendorId(vendorId);
        tx.setPartnerId(partnerId);
        tx.setAmount(new BigDecimal("100.00"));
    }

    private ProfitShareRule rule(BigDecimal vendorPct, BigDecimal companyPct, BigDecimal partnerPct) {
        ProfitShareRule r = new ProfitShareRule();
        r.setVendorSharePct(vendorPct);
        r.setCompanySharePct(companyPct);
        r.setPartnerSharePct(partnerPct);
        return r;
    }

    @Test
    void usesExactMatchWhenPresent_withoutFallingBackFurther() {
        when(ruleRepository.findFirstByVendorIdAndBundleIdAndPartnerId(vendorId, bundleId, partnerId))
                .thenReturn(Optional.of(rule(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("20"))));

        ProfitShareCalculationService.SplitResult split = service.calculateSplit(tx);

        assertThat(split.vendorAmount()).isEqualByComparingTo("50.00");
        assertThat(split.companyAmount()).isEqualByComparingTo("30.00");
        assertThat(split.partnerAmount()).isEqualByComparingTo("20.00");
        verify(ruleRepository, never()).findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(any());
        verify(ruleRepository, never()).findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(any());
    }

    @Test
    void fallsBackToBundleLevelRuleWhenNoExactMatch() {
        when(ruleRepository.findFirstByVendorIdAndBundleIdAndPartnerId(vendorId, bundleId, partnerId))
                .thenReturn(Optional.empty());
        when(ruleRepository.findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(bundleId))
                .thenReturn(Optional.of(rule(new BigDecimal("40"), new BigDecimal("40"), new BigDecimal("20"))));

        ProfitShareCalculationService.SplitResult split = service.calculateSplit(tx);

        assertThat(split.vendorAmount()).isEqualByComparingTo("40.00");
        verify(ruleRepository, never()).findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(any());
    }

    @Test
    void fallsBackToEcoSystemLevelRuleWhenNoExactOrBundleMatch() {
        when(ruleRepository.findFirstByVendorIdAndBundleIdAndPartnerId(vendorId, bundleId, partnerId))
                .thenReturn(Optional.empty());
        when(ruleRepository.findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(bundleId))
                .thenReturn(Optional.empty());
        when(ruleRepository.findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(ecoSystemId))
                .thenReturn(Optional.of(rule(new BigDecimal("60"), new BigDecimal("25"), new BigDecimal("15"))));

        ProfitShareCalculationService.SplitResult split = service.calculateSplit(tx);

        assertThat(split.vendorAmount()).isEqualByComparingTo("60.00");
        assertThat(split.companyAmount()).isEqualByComparingTo("25.00");
        assertThat(split.partnerAmount()).isEqualByComparingTo("15.00");
    }

    @Test
    void throwsWhenNoRuleMatchesAtAnyTier() {
        when(ruleRepository.findFirstByVendorIdAndBundleIdAndPartnerId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(ruleRepository.findFirstByBundleIdAndVendorIdIsNullAndPartnerIdIsNull(any()))
                .thenReturn(Optional.empty());
        when(ruleRepository.findFirstByEcoSystemIdAndBundleIdIsNullAndVendorIdIsNullAndPartnerIdIsNull(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateSplit(tx))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No ProfitShareRule configured");
    }
}
