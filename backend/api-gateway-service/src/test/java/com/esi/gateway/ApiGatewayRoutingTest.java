package com.esi.gateway;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayRoutingTest {

    private static final MockWebServer authServer = new MockWebServer();
    private static final MockWebServer profileServer = new MockWebServer();
    private static final MockWebServer bookingServer = new MockWebServer();
    private static final MockWebServer paymentServer = new MockWebServer();
    private static final MockWebServer discoveryServer = new MockWebServer();
    private static final MockWebServer reviewServer = new MockWebServer();
    private static final MockWebServer geolocationServer = new MockWebServer();
    private static final MockWebServer validationServer = new MockWebServer();
    private static boolean serversStarted = false;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void setUpServers() throws IOException {
        ensureServersStarted();
    }

    @AfterAll
    static void tearDownServers() throws IOException {
        authServer.shutdown();
        profileServer.shutdown();
        bookingServer.shutdown();
        paymentServer.shutdown();
        discoveryServer.shutdown();
        reviewServer.shutdown();
        geolocationServer.shutdown();
        validationServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        ensureServersStarted();
        registry.add("AUTH_SERVICE_URL", () -> authServer.url("/").toString());
        registry.add("PROFILE_SERVICE_URL", () -> profileServer.url("/").toString());
        registry.add("BOOKING_SERVICE_URL", () -> bookingServer.url("/").toString());
        registry.add("PAYMENT_SERVICE_URL", () -> paymentServer.url("/").toString());
        registry.add("DISCOVERY_SERVICE_URL", () -> discoveryServer.url("/").toString());
        registry.add("REVIEW_SERVICE_URL", () -> reviewServer.url("/").toString());
        registry.add("GEOLOCATION_SERVICE_URL", () -> geolocationServer.url("/").toString());
        registry.add("VALIDATION_SERVICE_URL", () -> validationServer.url("/").toString());
    }

    private static synchronized void ensureServersStarted() {
        if (serversStarted) {
            return;
        }
        try {
            authServer.start();
            profileServer.start();
            bookingServer.start();
            paymentServer.start();
            discoveryServer.start();
            reviewServer.start();
            geolocationServer.start();
            validationServer.start();
            serversStarted = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start mock downstream servers", e);
        }
    }

    @Test
    void authRoute_forwardsRequestToAuthService() throws InterruptedException {
        authServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"valid\":true}"));

        webTestClient.get()
                .uri("/api/auth/validate")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.valid").isEqualTo(true);

        RecordedRequest request = authServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/auth/validate");
    }

    @Test
    void ridesRoute_forwardsRequestToBookingService() throws InterruptedException {
        bookingServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true}"));

        webTestClient.get()
                .uri("/api/rides")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true);

        RecordedRequest request = bookingServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/rides");
    }

    @Test
    void paymentRoute_forwardsRequestToPaymentService() throws InterruptedException {
        paymentServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true}"));

        webTestClient.get()
                .uri("/api/payments/test-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true);

        RecordedRequest request = paymentServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/payments/test-id");
    }

    @Test
    void discoveryRoute_forwardsRequestToDiscoveryService() throws InterruptedException {
        discoveryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"COMPLETED\"}"));

        webTestClient.get()
                .uri("/api/search?originLat=59.4&originLon=24.7&destinationLat=58.4&destinationLon=26.7")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("COMPLETED");

        RecordedRequest request = discoveryServer.takeRequest();
        assertThat(request.getPath()).startsWith("/search?");
    }
}
