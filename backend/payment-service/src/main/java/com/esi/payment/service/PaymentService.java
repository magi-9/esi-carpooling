package com.esi.payment.service;

import com.esi.payment.client.BookingClient;
import com.esi.payment.domain.Money;
import com.esi.payment.domain.Payment;
import com.esi.payment.domain.PaymentRepository;
import com.esi.payment.domain.Refund;
import com.esi.payment.dto.InitiatePaymentRequest;
import com.esi.payment.dto.MoneyResponse;
import com.esi.payment.dto.PaymentResponse;
import com.esi.payment.dto.RefundRequest;
import com.esi.payment.dto.RefundResponse;
import com.esi.payment.exception.BookingNotFoundException;
import com.esi.payment.exception.InvalidStateTransitionException;
import com.esi.payment.exception.PaymentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;

    public PaymentService(PaymentRepository paymentRepository, BookingClient bookingClient) {
        this.paymentRepository = paymentRepository;
        this.bookingClient = bookingClient;
    }

    public PaymentResponse initiatePayment(InitiatePaymentRequest req, String authHeader) {
        boolean exists = bookingClient.bookingExists(req.bookingId(), authHeader);
        if (!exists) {
            throw new BookingNotFoundException("Booking not found: " + req.bookingId());
        }
        Money money = Money.of(req.amount(), req.currency());
        Payment payment = Payment.initiate(req.bookingId(), req.payerId(), req.payeeId(), money);
        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public PaymentResponse authorizePayment(InitiatePaymentRequest req, String authHeader) {
        boolean exists = bookingClient.bookingExists(req.bookingId(), authHeader);
        if (!exists) {
            throw new BookingNotFoundException("Booking not found: " + req.bookingId());
        }
        Money money = Money.of(req.amount(), req.currency());
        Payment payment = Payment.authorize(req.bookingId(), req.payerId(), req.payeeId(), money);
        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public List<PaymentResponse> getPaymentsByUser(String payerId) {
        return paymentRepository.findByPayerId(payerId).stream().map(this::toResponse).toList();
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    public PaymentResponse completePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        try {
            payment.complete();
        } catch (IllegalStateException e) {
            throw new InvalidStateTransitionException(e.getMessage());
        }
        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public RefundResponse requestRefund(UUID paymentId, RefundRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        try {
            payment.requestRefund(req.reason());
        } catch (IllegalStateException e) {
            throw new InvalidStateTransitionException(e.getMessage());
        }
        Payment saved = paymentRepository.save(payment);
        return toRefundResponse(saved.getRefund());
    }

    public RefundResponse getRefund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        if (payment.getRefund() == null) {
            throw new PaymentNotFoundException("No refund found for payment: " + paymentId);
        }
        return toRefundResponse(payment.getRefund());
    }

    private PaymentResponse toResponse(Payment p) {
        RefundResponse refundResponse = p.getRefund() != null ? toRefundResponse(p.getRefund()) : null;
        return new PaymentResponse(
                p.getPaymentId(), p.getBookingId(), p.getPayerId(), p.getPayeeId(),
                new MoneyResponse(p.getChargedAmount().amount(), p.getChargedAmount().currency()),
                p.getStatus().name(), p.getCreatedAt(), p.getCompletedAt(), refundResponse
        );
    }

    private RefundResponse toRefundResponse(Refund r) {
        return new RefundResponse(r.getRefundId(),
                new MoneyResponse(r.getRefundedAmount().amount(), r.getRefundedAmount().currency()),
                r.getReason(), r.getProcessedAt());
    }
}
