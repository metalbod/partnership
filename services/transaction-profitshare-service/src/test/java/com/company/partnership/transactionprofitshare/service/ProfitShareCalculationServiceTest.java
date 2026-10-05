package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.domain.OfferingLine;
import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.ProfitShareReportResponse;
import com.company.partnership.transactionprofitshare.repository.ProfitShareReportRepository;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitShareCalculationServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ProfitShareReportRepository reportRepository;

    private Transaction tx(String amount, String vendor, String partner, String company) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.getOfferingLines().add(new OfferingLine(UUID.randomUUID(), "Offering", UUID.randomUUID(),
                OfferingLine.PriceType.FIXED, new BigDecimal(vendor), new BigDecimal(vendor)));
        t.setPartnerAmount(new BigDecimal(partner));
        t.setCompanyAmount(new BigDecimal(company));
        return t;
    }

    @Test
    void sumsEachTransactionsOwnSnapshotAndMarksThemReported() {
        Transaction a = tx("2000.00", "620.00", "300.00", "1080.00");
        Transaction b = tx("480.00", "65.00", "50.00", "365.00");
        when(transactionRepository.findReportable(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(a, b));
        when(reportRepository.save(any(ProfitShareReport.class))).thenAnswer(inv -> {
            ProfitShareReport r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(transactionRepository.vendorTotalsForReport(any())).thenReturn(List.of());

        ProfitShareReportResponse report = new ProfitShareCalculationService(transactionRepository, reportRepository)
                .runPeriodicCalculation(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(report.transactionCount()).isEqualTo(2);
        assertThat(report.totalAmount()).isEqualByComparingTo("2480.00");
        assertThat(report.vendorTotal()).isEqualByComparingTo("685.00");
        assertThat(report.partnerTotal()).isEqualByComparingTo("350.00");
        assertThat(report.companyTotal()).isEqualByComparingTo("1445.00");
        assertThat(a.getIncludedInReportId()).isEqualTo(report.id());
        assertThat(b.getIncludedInReportId()).isEqualTo(report.id());
    }

    @Test
    void returnsNothingWhenThereIsNothingToReport() {
        when(transactionRepository.findReportable(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());
        assertThat(new ProfitShareCalculationService(transactionRepository, reportRepository)
                .runPeriodicCalculation(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).isNull();
    }
}
