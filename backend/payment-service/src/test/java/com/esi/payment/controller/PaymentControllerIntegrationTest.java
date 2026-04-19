package com.esi.payment.controller;

import com.esi.payment.client.BookingClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BookingClient bookingClient;

    @Test
    void initiatePayment_returnsCreated() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-test-1",
                "payerId", "payer-1",
                "payeeId", "payee-1",
                "amount", 25.00,
                "currency", "EUR"
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.bookingId").value("booking-test-1"));
    }

    @Test
    void authorizePayment_returnsCreatedProcessing() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-auth-1",
                "payerId", "payer-1",
                "payeeId", "payee-1",
                "amount", 25.00,
                "currency", "EUR"
        );

        mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.completedAt").doesNotExist());
    }

    @Test
    void initiatePayment_missingField_returnsBadRequest() throws Exception {
        Map<String, Object> body = Map.of("bookingId", "b1");
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_notFound_returns404() throws Exception {
        mockMvc.perform(get("/payments/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
        void fullPaymentLifecycle_initiateThenGet() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-lifecycle",
                "payerId", "payer-x",
                "payeeId", "payee-x",
                "amount", new BigDecimal("30.00"),
                "currency", "EUR"
        );

        String createResult = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(createResult).get("paymentId").asText();

                mockMvc.perform(get("/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
        void requestRefund_onProcessing_returnsConflict() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-refund-fail",
                "payerId", "p1", "payeeId", "p2",
                "amount", 10, "currency", "EUR"
        );

        String createResult = mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(createResult).get("paymentId").asText();

        mockMvc.perform(post("/payments/" + paymentId + "/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"test\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void refundLifecycle_completeThenRefundThenFetchRefund_returnsCreatedAndOk() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-refund-success",
                "payerId", "payer-2",
                "payeeId", "payee-2",
                "amount", 42.50,
                "currency", "EUR"
        );

        String createResult = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(createResult).get("paymentId").asText();

        mockMvc.perform(post("/payments/" + paymentId + "/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"driver issue\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("driver issue"))
                .andExpect(jsonPath("$.refundedAmount.amount").value(42.5));

        mockMvc.perform(post("/payments/" + paymentId + "/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"duplicate\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/payments/" + paymentId + "/refunds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("driver issue"));
    }

    @Test
    void getRefund_missingRefund_returns404() throws Exception {
        when(bookingClient.bookingExists(anyString(), any())).thenReturn(true);

        Map<String, Object> body = Map.of(
                "bookingId", "booking-no-refund",
                "payerId", "payer-3",
                "payeeId", "payee-3",
                "amount", 18,
                "currency", "EUR"
        );

        String createResult = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(createResult).get("paymentId").asText();

        mockMvc.perform(get("/payments/" + paymentId + "/refunds"))
                .andExpect(status().isNotFound());
    }
}
