package com.esi.payment.event;

import com.esi.payment.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventPublisher {

    public static final String PAYMENT_SUCCESSFUL_TOPIC = "payment-successful-topic";
    public static final String PAYMENT_REFUNDED_TOPIC = "payment-refunded-topic";

    private final KafkaTemplate<String, PaymentSuccessfulEvent> successTemplate;
    private final KafkaTemplate<String, PaymentRefundedEvent> refundTemplate;

    public PaymentEventPublisher(
            KafkaTemplate<String, PaymentSuccessfulEvent> successTemplate,
            KafkaTemplate<String, PaymentRefundedEvent> refundTemplate) {
        this.successTemplate = successTemplate;
        this.refundTemplate = refundTemplate;
    }

    public void publishPaymentSuccessful(Payment payment) {
        PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getPayerId(),
                payment.getPayeeId(),
                payment.getChargedAmount().amount(),
                payment.getChargedAmount().currency(),
                payment.getCompletedAt()
        );

        try {
            successTemplate
                    .send(PAYMENT_SUCCESSFUL_TOPIC, payment.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish PaymentSuccessfulEvent for payment {}: {}",
                                    payment.getPaymentId(), ex.getMessage());
                        } else {
                            log.debug("Published PaymentSuccessfulEvent for payment {}",
                                    payment.getPaymentId());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Skipping PaymentSuccessfulEvent for payment {} because Kafka is unavailable: {}",
                    payment.getPaymentId(), ex.getMessage());
        }
    }

    public void publishPaymentRefunded(Payment payment) {
        if (payment.getRefund() == null) return;
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getRefund().getRefundId().toString()
        );
        try {
            refundTemplate
                    .send(PAYMENT_REFUNDED_TOPIC, payment.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish PaymentRefundedEvent for payment {}: {}",
                                    payment.getPaymentId(), ex.getMessage());
                        } else {
                            log.debug("Published PaymentRefundedEvent for payment {}",
                                    payment.getPaymentId());
                        }
                    });
        } catch (Exception ex) {
            log.warn("Skipping PaymentRefundedEvent for payment {} because Kafka is unavailable: {}",
                    payment.getPaymentId(), ex.getMessage());
        }
    }
}
