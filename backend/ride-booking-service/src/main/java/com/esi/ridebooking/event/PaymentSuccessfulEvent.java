package com.esi.ridebooking.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSuccessfulEvent(
        UUID paymentId,
        String bookingId,
        String payerId,
        String payeeId,
        BigDecimal amount,
        String currency,
        Instant completedAt
) {
}
