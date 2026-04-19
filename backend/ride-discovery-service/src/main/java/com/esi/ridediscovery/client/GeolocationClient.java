package com.esi.ridediscovery.client;

import com.esi.ridediscovery.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GeolocationClient {

    private static final Logger log = LoggerFactory.getLogger(GeolocationClient.class);

    private final RestClient restClient;

    public GeolocationClient(
            RestClient.Builder restClientBuilder,
            @Value("${clients.geolocation-service-url}") String geolocationServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(geolocationServiceUrl).build();
    }

    public Location geocode(String address, String authHeader) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/geocode").queryParam("address", address).build())
                    .header("Authorization", authHeader != null ? authHeader : "")
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("latitude") == null || response.get("longitude") == null) {
                throw new IllegalStateException("Invalid geocode response");
            }

            double latitude = ((Number) response.get("latitude")).doubleValue();
            double longitude = ((Number) response.get("longitude")).doubleValue();
            String displayAddress = String.valueOf(response.getOrDefault("displayAddress", address));
            return new Location(latitude, longitude, displayAddress);
        } catch (Exception e) {
            log.warn("Failed to geocode address '{}': {}", address, e.getMessage());
            throw new IllegalArgumentException("Could not resolve address: " + address);
        }
    }
}
