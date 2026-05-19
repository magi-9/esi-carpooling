package com.esi.ridebooking.event;

import java.util.UUID;

public record PaymentRefundedEvent(
        UUID paymentId,
        String bookingId,
        String refundId
) {
}
