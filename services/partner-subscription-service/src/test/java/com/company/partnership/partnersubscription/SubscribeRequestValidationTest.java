package com.company.partnership.partnersubscription;

import com.company.partnership.partnersubscription.domain.PartnerSubscription.ShareType;
import com.company.partnership.partnersubscription.dto.SubscribeRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubscribeRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private SubscribeRequest req(String price, ShareType type, String share) {
        return new SubscribeRequest(UUID.randomUUID(), UUID.randomUUID(), 1,
                new BigDecimal(price), type, new BigDecimal(share));
    }

    @Test
    void acceptsPercentageAndFixedShareWithinLimits() {
        assertThat(validator.validate(req("2150.00", ShareType.PERCENTAGE, "20"))).isEmpty();
        assertThat(validator.validate(req("2150.00", ShareType.FIXED, "300"))).isEmpty();
    }

    @Test
    void rejectsShareAboveTheLimit() {
        assertThat(validator.validate(req("2150.00", ShareType.PERCENTAGE, "101"))).isNotEmpty();
        assertThat(validator.validate(req("100.00", ShareType.FIXED, "150"))).isNotEmpty();
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThat(validator.validate(req("0", ShareType.FIXED, "0"))).isNotEmpty();
    }
}
