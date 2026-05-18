package com.esi.payment.controller;

import com.esi.payment.dto.InitiatePaymentRequest;
import com.esi.payment.dto.PaymentResponse;
import com.esi.payment.dto.RefundRequest;
import com.esi.payment.dto.RefundResponse;
import com.esi.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment lifecycle and refund operations")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Initiate a payment", description = "Creates a completed payment after validating the booking.")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        PaymentResponse response = paymentService.initiatePayment(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get payments by user", description = "Returns all payments where the given user is the payer.")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@RequestParam String payerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(payerId));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details", description = "Returns a payment by its identifier.")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @PatchMapping("/{paymentId}/complete")
    @Operation(summary = "Complete a payment", description = "Marks a processing payment as completed.")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.completePayment(paymentId));
    }

    @PostMapping("/authorize")
    @Operation(summary = "Authorize a payment", description = "Creates a processing payment that can be captured later.")
    public ResponseEntity<PaymentResponse> authorizePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        PaymentResponse response = paymentService.authorizePayment(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{paymentId}/refunds")
    @Operation(summary = "Request a refund", description = "Creates a refund for a completed payment.")
    public ResponseEntity<RefundResponse> requestRefund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RefundRequest request) {
        RefundResponse response = paymentService.requestRefund(paymentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}/refunds")
    @Operation(summary = "Get refund details", description = "Returns the refund linked to a payment.")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getRefund(paymentId));
    }
}
