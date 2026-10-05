package com.company.partnership.transactionprofitshare.pricing;

import com.company.partnership.transactionprofitshare.exception.UnprocessableTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Splits one bundle sale three ways. Pure arithmetic, no I/O.
 *
 *  - each offering's vendor share is its own unit price: a FIXED MYR amount, or a
 *    PERCENTAGE of the bundle cost;
 *  - the partner's share is agreed per partner programme, same two forms;
 *  - the company keeps what remains. If the vendors and partner together exceed the
 *    cost, the terms can't be applied and the sale is rejected rather than recorded
 *    with a negative company share.
 */
public final class PricingCalculator {

    private PricingCalculator() {
    }

    public enum Kind {
        FIXED, PERCENTAGE
    }

    public record OfferingTerms(UUID offeringId, String offeringName, UUID vendorId, Kind kind, BigDecimal value) {
    }

    public record OfferingShare(OfferingTerms terms, BigDecimal amount) {
    }

    public record Breakdown(BigDecimal price, List<OfferingShare> vendorShares, BigDecimal vendorTotal,
                            BigDecimal partnerAmount, BigDecimal companyAmount) {
    }

    public static Breakdown compute(BigDecimal price, Kind partnerKind, BigDecimal partnerValue, List<OfferingTerms> offerings) {
        List<OfferingShare> shares = new ArrayList<>();
        BigDecimal vendorTotal = BigDecimal.ZERO;
        for (OfferingTerms o : offerings) {
            BigDecimal amount = amountOf(price, o.kind(), o.value());
            shares.add(new OfferingShare(o, amount));
            vendorTotal = vendorTotal.add(amount);
        }
        BigDecimal partner = amountOf(price, partnerKind, partnerValue);
        BigDecimal company = price.subtract(vendorTotal).subtract(partner);
        if (company.signum() < 0) {
            throw new UnprocessableTransactionException(
                    "The vendors' shares (" + vendorTotal + ") and the partner's share (" + partner
                            + ") add up to more than the bundle cost (" + price + "). Adjust the partner's terms or the offerings' prices.");
        }
        return new Breakdown(price, shares, vendorTotal, partner, company);
    }

    private static BigDecimal amountOf(BigDecimal price, Kind kind, BigDecimal value) {
        BigDecimal raw = kind == Kind.FIXED ? value : price.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return raw.setScale(2, RoundingMode.HALF_UP);
    }
}
