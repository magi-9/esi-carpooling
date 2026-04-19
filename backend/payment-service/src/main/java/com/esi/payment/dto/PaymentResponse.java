package com.esi.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String bookingId,
        String payerId,
        String payeeId,
        MoneyResponse chargedAmount,
        String status,
        Instant createdAt,
        Instant completedAt,
        RefundResponse refund
) {}
