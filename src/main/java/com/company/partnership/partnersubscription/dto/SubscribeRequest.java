package com.company.partnership.partnersubscription.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** FR-PTR-02: subscribe a partner to a WHOLE bundle version \u2013 no offering-level selection field exists by design. */
public record SubscribeRequest(
        @NotNull UUID partnerId,
        @NotNull UUID bundleId,
        @NotNull Integer bundleVersion
) {
}
