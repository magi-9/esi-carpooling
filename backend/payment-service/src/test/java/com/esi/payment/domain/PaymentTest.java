package com.esi.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private Money testMoney() {
        return Money.of(BigDecimal.valueOf(25), "EUR");
    }

    @Test
    void initiate_createsPaymentInCompletedStatus() {
        Payment payment = Payment.initiate("booking-1", "payer-1", "payee-1", testMoney());
        assertThat(payment.getPaymentId()).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getRefund()).isNull();
        assertThat(payment.getCompletedAt()).isNotNull();
    }

    @Test
    void authorize_createsPaymentInProcessingStatus() {
        Payment payment = Payment.authorize("booking-1", "payer-1", "payee-1", testMoney());
        assertThat(payment.getPaymentId()).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(payment.getRefund()).isNull();
        assertThat(payment.getCompletedAt()).isNull();
    }

    @Test
    void complete_transitionsToCompleted() {
        Payment payment = Payment.authorize("b1", "p1", "p2", testMoney());
        payment.complete();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_fromFailed_throwsIllegalState() {
        Payment payment = Payment.authorize("b1", "p1", "p2", testMoney());
        payment.fail();
        assertThatThrownBy(payment::complete).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requestRefund_fromCompleted_transitionsToRefunded() {
        Payment payment = Payment.initiate("b1", "p1", "p2", testMoney());
        payment.requestRefund("Changed mind");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefund()).isNotNull();
        assertThat(payment.getRefund().getReason()).isEqualTo("Changed mind");
        assertThat(payment.getRefund().getRefundedAmount()).isEqualTo(testMoney());
    }

    @Test
    void requestRefund_fromProcessing_throwsIllegalState() {
        Payment payment = Payment.authorize("b1", "p1", "p2", testMoney());
        assertThatThrownBy(() -> payment.requestRefund("reason")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fail_transitionsToFailed() {
        Payment payment = Payment.authorize("b1", "p1", "p2", testMoney());
        payment.fail();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void fail_fromCompleted_throwsIllegalState() {
        Payment payment = Payment.initiate("b1", "p1", "p2", testMoney());
        assertThatThrownBy(payment::fail).isInstanceOf(IllegalStateException.class);
    }
}
