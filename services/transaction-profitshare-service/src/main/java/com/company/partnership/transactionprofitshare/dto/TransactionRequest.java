package com.company.partnership.transactionprofitshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A customer (e.g. a student) subscribing to a partner's whole bundle. There is no
 * offering-level field: customers take the entire bundle, never a single offering
 * (BRD whole-bundle rule). Policy fields are optional and only relevant when the
 * bundle includes an insurance offering (BRD Section 6.2).
 */
public record TransactionRequest(
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        @NotBlank @Email String customerEmail,
        @NotNull UUID partnerId,
        @NotNull UUID bundleId,
        @NotNull UUID ecoSystemId,
        @NotNull @Positive BigDecimal amount,
        BigDecimal premium,
        BigDecimal sumInsured,
        String policyNumber
) {
}
