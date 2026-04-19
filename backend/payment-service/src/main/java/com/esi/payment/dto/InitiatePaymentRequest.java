package com.esi.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InitiatePaymentRequest(
        @NotBlank String bookingId,
        @NotBlank String payerId,
        @NotBlank String payeeId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency
) {}
