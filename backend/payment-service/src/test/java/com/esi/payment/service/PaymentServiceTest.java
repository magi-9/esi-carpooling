package com.esi.payment.service;

import com.esi.payment.client.BookingClient;
import com.esi.payment.domain.Money;
import com.esi.payment.domain.Payment;
import com.esi.payment.domain.PaymentRepository;
import com.esi.payment.domain.PaymentStatus;
import com.esi.payment.dto.InitiatePaymentRequest;
import com.esi.payment.dto.PaymentResponse;
import com.esi.payment.dto.RefundRequest;
import com.esi.payment.dto.RefundResponse;
import com.esi.payment.exception.BookingNotFoundException;
import com.esi.payment.exception.InvalidStateTransitionException;
import com.esi.payment.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingClient bookingClient;

    @InjectMocks
    private PaymentService paymentService;

    private InitiatePaymentRequest buildRequest() {
        return new InitiatePaymentRequest("booking-1", "payer-1", "payee-1",
                BigDecimal.valueOf(50), "EUR");
    }

    @Test
    void initiatePayment_bookingExists_createsPayment() {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.initiatePayment(buildRequest(), "Bearer token");

        assertThat(response.bookingId()).isEqualTo("booking-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED.name());
        assertThat(response.chargedAmount().amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(paymentRepository).save(any());
    }

    @Test
    void authorizePayment_bookingExists_createsProcessingPayment() {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.authorizePayment(buildRequest(), "Bearer token");

        assertThat(response.bookingId()).isEqualTo("booking-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.PROCESSING.name());
        assertThat(response.completedAt()).isNull();
        verify(paymentRepository).save(any());
    }

    @Test
    void initiatePayment_bookingNotFound_throwsBookingNotFoundException() {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> paymentService.initiatePayment(buildRequest(), null))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getPayment_notFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(UUID.randomUUID()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void completePayment_notFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.completePayment(UUID.randomUUID()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void completePayment_initiatedPayment_completesIt() {
        Payment payment = Payment.authorize("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.completePayment(payment.getPaymentId());

        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED.name());
        assertThat(response.completedAt()).isNotNull();
        verify(paymentRepository).save(payment);
    }

    @Test
    void completePayment_alreadyFailed_throwsInvalidStateTransition() {
        Payment payment = Payment.authorize("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        payment.fail();
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.completePayment(payment.getPaymentId()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void requestRefund_completedPayment_createsRefund() {
        Payment payment = Payment.initiate("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefundResponse response = paymentService.requestRefund(payment.getPaymentId(),
                new RefundRequest("Driver was late"));

        assertThat(response.reason()).isEqualTo("Driver was late");
        assertThat(response.refundedAmount().amount()).isEqualByComparingTo(BigDecimal.TEN);
        verify(paymentRepository).save(payment);
    }

    @Test
    void requestRefund_processingPayment_throwsInvalidStateTransition() {
        Payment payment = Payment.authorize("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.requestRefund(
                payment.getPaymentId(), new RefundRequest("Driver was late")))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void getRefund_paymentWithRefund_returnsRefundResponse() {
        Payment payment = Payment.initiate("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        payment.requestRefund("Late pickup");
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        RefundResponse response = paymentService.getRefund(payment.getPaymentId());

        assertThat(response.reason()).isEqualTo("Late pickup");
        assertThat(response.refundedAmount().amount()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void getRefund_paymentWithoutRefund_throwsPaymentNotFoundException() {
        Payment payment = Payment.initiate("b1", "p1", "p2", Money.of(BigDecimal.TEN, "EUR"));
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getRefund(payment.getPaymentId()))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("No refund found");
    }
}
