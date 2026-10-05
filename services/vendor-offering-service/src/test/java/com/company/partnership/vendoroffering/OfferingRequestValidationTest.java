package com.company.partnership.vendoroffering;

import com.company.partnership.vendoroffering.domain.Offering;
import com.company.partnership.vendoroffering.dto.OfferingRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OfferingRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private OfferingRequest request(Offering.PriceType type, String value) {
        return new OfferingRequest(UUID.randomUUID(), "Student SIM", null,
                Offering.OfferingType.NON_INSURANCE, type, new BigDecimal(value));
    }

    @Test
    void acceptsFixedAmountAndPercentageWithinRange() {
        assertThat(validator.validate(request(Offering.PriceType.FIXED, "120.00"))).isEmpty();
        assertThat(validator.validate(request(Offering.PriceType.PERCENTAGE, "12.5"))).isEmpty();
    }

    @Test
    void rejectsPercentageAbove100() {
        assertThat(validator.validate(request(Offering.PriceType.PERCENTAGE, "100.01"))).isNotEmpty();
    }

    @Test
    void rejectsNegativePrice() {
        assertThat(validator.validate(request(Offering.PriceType.FIXED, "-1"))).isNotEmpty();
    }
}
