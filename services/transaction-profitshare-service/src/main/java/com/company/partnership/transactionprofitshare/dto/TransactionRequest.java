package com.company.partnership.transactionprofitshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A customer (e.g. a student) taking a partner's whole bundle. The caller names only the
 * customer and the partner programme (subscriptionId); the price, the bundle and its
 * offerings, and the profit-share breakdown are all resolved from the agreed terms and
 * snapshotted. Policy fields are optional, for bundles that include insurance (BRD 6.2).
 */
public record TransactionRequest(
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        @NotBlank @Email String customerEmail,
        @NotNull UUID subscriptionId,
        BigDecimal premium,
        BigDecimal sumInsured,
        String policyNumber
) {
}
