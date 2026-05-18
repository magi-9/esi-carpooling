package com.esi.ridediscovery.client;

import com.esi.ridediscovery.client.dto.RideDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class RideBookingClient {

    private static final Logger log = LoggerFactory.getLogger(RideBookingClient.class);

    private final RestClient restClient;

    public RideBookingClient(
            RestClient.Builder restClientBuilder,
            @Value("${clients.booking-service-url}") String bookingServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(bookingServiceUrl).build();
    }

    public List<RideDto> searchRides(
            String originLat,
            String originLon,
            String destLat,
            String destLon,
            String departureDate,
            int seatsNeeded,
            BigDecimal maxPricePerSeat,
            String authHeader) {
        try {
            List<RideDto> rides = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rides")
                            .queryParam("originLat", originLat)
                            .queryParam("originLon", originLon)
                            .queryParam("destinationLat", destLat)
                            .queryParam("destinationLon", destLon)
                            .queryParam("departureDate", departureDate)
                            .queryParam("seatsNeeded", seatsNeeded)
                            .queryParamIfPresent("maxPricePerSeat", Optional.ofNullable(maxPricePerSeat))
                            .build())
                    // Do not forward auth header - GET /rides is public on ride-booking-service
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RideDto>>() {});
            return rides != null ? rides : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch rides from booking service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
