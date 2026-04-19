package com.esi.payment.controller;

import com.esi.payment.dto.InitiatePaymentRequest;
import com.esi.payment.dto.PaymentResponse;
import com.esi.payment.dto.RefundRequest;
import com.esi.payment.dto.RefundResponse;
import com.esi.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        PaymentResponse response = paymentService.initiatePayment(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorizePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        PaymentResponse response = paymentService.authorizePayment(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{paymentId}/refunds")
    public ResponseEntity<RefundResponse> requestRefund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RefundRequest request) {
        RefundResponse response = paymentService.requestRefund(paymentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}/refunds")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getRefund(paymentId));
    }
}
