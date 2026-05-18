package com.esi.payment.event;

import com.esi.payment.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventPublisher {

    public static final String PAYMENT_SUCCESSFUL_TOPIC = "payment-successful-topic";

    private final KafkaTemplate<String, PaymentSuccessfulEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentSuccessfulEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
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
            kafkaTemplate
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
}
