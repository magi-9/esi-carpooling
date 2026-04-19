package com.esi.payment.infrastructure;

import com.esi.payment.domain.Money;
import com.esi.payment.domain.Payment;
import com.esi.payment.domain.PaymentStatus;
import com.esi.payment.domain.Refund;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toJpa(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId(payment.getPaymentId());
        entity.setBookingId(payment.getBookingId());
        entity.setPayerId(payment.getPayerId());
        entity.setPayeeId(payment.getPayeeId());
        entity.setAmount(payment.getChargedAmount().amount());
        entity.setCurrency(payment.getChargedAmount().currency());
        entity.setStatus(payment.getStatus().name());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setCompletedAt(payment.getCompletedAt());

        if (payment.getRefund() != null) {
            Refund refund = payment.getRefund();
            entity.setRefundId(refund.getRefundId());
            entity.setRefundAmount(refund.getRefundedAmount().amount());
            entity.setRefundCurrency(refund.getRefundedAmount().currency());
            entity.setRefundReason(refund.getReason());
            entity.setRefundProcessedAt(refund.getProcessedAt());
        }
        return entity;
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        Money chargedAmount = Money.of(entity.getAmount(), entity.getCurrency());
        PaymentStatus status = PaymentStatus.valueOf(entity.getStatus());

        Refund refund = null;
        if (entity.getRefundId() != null) {
            Money refundedAmount = Money.of(entity.getRefundAmount(), entity.getRefundCurrency());
            refund = Refund.reconstitute(entity.getRefundId(), refundedAmount,
                    entity.getRefundReason(), entity.getRefundProcessedAt());
        }

        return Payment.reconstitute(
                entity.getPaymentId(),
                entity.getBookingId(),
                entity.getPayerId(),
                entity.getPayeeId(),
                chargedAmount,
                status,
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                refund
        );
    }
}
