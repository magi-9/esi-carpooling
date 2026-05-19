package com.esi.ridebooking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.esi.ridebooking.bookings.Booking;
import com.esi.ridebooking.bookings.BookingRepository;

import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private static final String SUCCESS_TOPIC = "payment-successful-topic";
    private static final String REFUNDED_TOPIC = "payment-refunded-topic";

    private final BookingRepository bookingRepository;

    public PaymentEventConsumer(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    @KafkaListener(topics = SUCCESS_TOPIC, groupId = "ride-booking-service")
    public void onPaymentSuccessful(PaymentSuccessfulEvent event) {
        log.info("Received PaymentSuccessfulEvent for payment: {}, booking: {}", 
                event.paymentId(), event.bookingId());

        try {
            UUID bookingId = UUID.fromString(event.bookingId());
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                log.warn("Booking {} not found for payment {}", event.bookingId(), event.paymentId());
                return;
            }

            if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
                log.warn("Booking {} is in status {}, expected PENDING or CONFIRMED", bookingId, booking.getStatus());
                return;
            }

            booking.setPaymentId(event.paymentId());
            booking.setStatus("PAID");
            bookingRepository.save(booking);
            log.info("Marked booking {} as PAID", bookingId);
        } catch (Exception e) {
            log.error("Failed to process PaymentSuccessfulEvent for booking {}: {}", 
                    event.bookingId(), e.getMessage());
        }
    }

    @Transactional
    @KafkaListener(topics = REFUNDED_TOPIC, groupId = "ride-booking-service")
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Received PaymentRefundedEvent for payment: {}, booking: {}", 
                event.paymentId(), event.bookingId());

        try {
            UUID bookingId = UUID.fromString(event.bookingId());
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                log.warn("Booking {} not found for refund payment {}", event.bookingId(), event.paymentId());
                return;
            }

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            log.info("Cancelled booking {} due to refund", bookingId);
        } catch (Exception e) {
            log.error("Failed to process PaymentRefundedEvent for booking {}: {}", 
                    event.bookingId(), e.getMessage());
        }
    }
}
