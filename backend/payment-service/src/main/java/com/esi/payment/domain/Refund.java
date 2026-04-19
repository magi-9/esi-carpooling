package com.esi.payment.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Refund {

    private final UUID refundId;
    private final Money refundedAmount;
    private final String reason;
    private final Instant processedAt;

    private Refund(UUID refundId, Money refundedAmount, String reason, Instant processedAt) {
        this.refundId = refundId;
        this.refundedAmount = refundedAmount;
        this.reason = reason;
        this.processedAt = processedAt;
    }

    public static Refund create(Money refundedAmount, String reason) {
        return new Refund(UUID.randomUUID(), refundedAmount, reason, Instant.now());
    }

    // For reconstitution from persistence
    public static Refund reconstitute(UUID refundId, Money refundedAmount, String reason, Instant processedAt) {
        return new Refund(refundId, refundedAmount, reason, processedAt);
    }

}
