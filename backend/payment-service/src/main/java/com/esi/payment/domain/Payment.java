package com.esi.payment.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Payment {

    private final UUID paymentId;
    private final String bookingId;
    private final String payerId;
    private final String payeeId;
    private final Money chargedAmount;
    private PaymentStatus status;
    private final Instant createdAt;
    private Instant completedAt;
    private Refund refund;

    private Payment(UUID paymentId, String bookingId, String payerId, String payeeId,
                    Money chargedAmount, PaymentStatus status, Instant createdAt,
                    Instant completedAt, Refund refund) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.chargedAmount = chargedAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.refund = refund;
    }

    public static Payment initiate(String bookingId, String payerId, String payeeId, Money chargedAmount) {
        return new Payment(UUID.randomUUID(), bookingId, payerId, payeeId,
                chargedAmount, PaymentStatus.COMPLETED, Instant.now(), Instant.now(), null);
    }

    public static Payment authorize(String bookingId, String payerId, String payeeId, Money chargedAmount) {
        return new Payment(UUID.randomUUID(), bookingId, payerId, payeeId,
                chargedAmount, PaymentStatus.PROCESSING, Instant.now(), null, null);
    }

    // For reconstitution from persistence
    public static Payment reconstitute(UUID paymentId, String bookingId, String payerId, String payeeId,
                                       Money chargedAmount, PaymentStatus status, Instant createdAt,
                                       Instant completedAt, Refund refund) {
        return new Payment(paymentId, bookingId, payerId, payeeId, chargedAmount, status,
                createdAt, completedAt, refund);
    }

    public void complete() {
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Cannot complete payment in status: " + status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void requestRefund(String reason) {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund payment in status: " + status);
        }
        this.refund = Refund.create(chargedAmount, reason);
        this.status = PaymentStatus.REFUNDED;
    }

    public void fail() {
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Cannot fail payment in status: " + status);
        }
        this.status = PaymentStatus.FAILED;
    }

}
