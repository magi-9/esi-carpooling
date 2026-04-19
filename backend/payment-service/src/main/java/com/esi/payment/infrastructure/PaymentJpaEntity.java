package com.esi.payment.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class PaymentJpaEntity {

    @Id
    @Column(name = "payment_id", columnDefinition = "UUID")
    private UUID paymentId;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "payer_id", nullable = false)
    private String payerId;

    @Column(name = "payee_id", nullable = false)
    private String payeeId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "refund_id", columnDefinition = "UUID")
    private UUID refundId;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    private BigDecimal refundAmount;

    @Column(name = "refund_currency", length = 10)
    private String refundCurrency;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "refund_processed_at")
    private Instant refundProcessedAt;

}
