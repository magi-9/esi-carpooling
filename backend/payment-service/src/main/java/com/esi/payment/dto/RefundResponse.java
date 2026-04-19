package com.esi.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record RefundResponse(UUID refundId, MoneyResponse refundedAmount, String reason, Instant processedAt) {}
