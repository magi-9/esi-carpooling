package com.esi.ridediscovery.client;

import com.esi.ridediscovery.client.dto.DriverRatingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ReviewClient {

    private static final Logger log = LoggerFactory.getLogger(ReviewClient.class);

    private final RestClient restClient;

    public ReviewClient(
            RestClient.Builder restClientBuilder,
            @Value("${clients.review-service-url}") String reviewServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(reviewServiceUrl).build();
    }

    public double getDriverRating(String driverId, String authHeader) {
        try {
            DriverRatingDto rating = restClient.get()
                    .uri("/reviews/drivers/{driverId}/rating", driverId)
                    .header("Authorization", authHeader != null ? authHeader : "")
                    .retrieve()
                    .body(DriverRatingDto.class);
            return rating != null ? rating.averageRating() : 0.0;
        } catch (Exception e) {
            log.warn("Failed to fetch driver rating for driverId={}: {}", driverId, e.getMessage());
            return 0.0;
        }
    }
}
