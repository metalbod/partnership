package com.company.partnership.transactionprofitshare.pricing;

import com.company.partnership.transactionprofitshare.exception.UnprocessableTransactionException;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.Breakdown;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.Kind;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.OfferingTerms;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingCalculatorTest {

    private static OfferingTerms offering(String name, Kind kind, String value) {
        return new OfferingTerms(UUID.randomUUID(), name, UUID.randomUUID(), kind, new BigDecimal(value));
    }

    @Test
    void mixesFixedAndPercentageVendorSharesAndLeavesTheRemainderToTheCompany() {
        // bundle costs 2,000: SIM fixed 120, transit 5% = 100, health 20% = 400; partner 15% = 300
        Breakdown b = PricingCalculator.compute(new BigDecimal("2000.00"), Kind.PERCENTAGE, new BigDecimal("15"), List.of(
                offering("SIM", Kind.FIXED, "120"), offering("Transit", Kind.PERCENTAGE, "5"), offering("Health", Kind.PERCENTAGE, "20")));

        assertThat(b.vendorShares()).extracting(s -> s.amount().toPlainString()).containsExactly("120.00", "100.00", "400.00");
        assertThat(b.vendorTotal()).isEqualByComparingTo("620.00");
        assertThat(b.partnerAmount()).isEqualByComparingTo("300.00");
        assertThat(b.companyAmount()).isEqualByComparingTo("1080.00");
        assertThat(b.vendorTotal().add(b.partnerAmount()).add(b.companyAmount())).isEqualByComparingTo(b.price());
    }

    @Test
    void fixedPartnerShareIsTakenAsAnAmount() {
        Breakdown b = PricingCalculator.compute(new BigDecimal("480.00"), Kind.FIXED, new BigDecimal("50"),
                List.of(offering("SIM", Kind.FIXED, "65")));
        assertThat(b.partnerAmount()).isEqualByComparingTo("50.00");
        assertThat(b.companyAmount()).isEqualByComparingTo("365.00");
    }

    @Test
    void roundsPercentagesToTheSen() {
        Breakdown b = PricingCalculator.compute(new BigDecimal("99.99"), Kind.PERCENTAGE, new BigDecimal("33.33"), List.of());
        assertThat(b.partnerAmount()).isEqualByComparingTo("33.33");
        assertThat(b.companyAmount()).isEqualByComparingTo("66.66");
    }

    @Test
    void rejectsTermsWhoseSharesExceedTheBundleCost() {
        assertThatThrownBy(() -> PricingCalculator.compute(new BigDecimal("100.00"), Kind.PERCENTAGE, new BigDecimal("60"),
                List.of(offering("SIM", Kind.FIXED, "50"))))
                .isInstanceOf(UnprocessableTransactionException.class)
                .hasMessageContaining("more than the bundle cost");
    }
}
