package com.esi.payment.client;

import com.esi.payment.exception.BookingServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class BookingClient {

    private static final Logger log = LoggerFactory.getLogger(BookingClient.class);

    private final RestClient restClient;

    public BookingClient(RestClient.Builder builder,
                         @Value("${clients.booking-service-url}") String bookingServiceUrl) {
        this.restClient = builder.baseUrl(bookingServiceUrl).build();
    }

    public boolean bookingExists(String bookingId, String authHeader) {
        try {
            restClient.get()
                    .uri("/bookings/{id}", bookingId)
                    .header("Authorization", authHeader != null ? authHeader : "")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new BookingServiceException("Booking service returned error: " + e.getStatusCode());
        } catch (Exception e) {
            log.warn("Could not verify booking {}: {}", bookingId, e.getMessage());
            throw new BookingServiceException("Booking service unavailable: " + e.getMessage());
        }
    }
}
