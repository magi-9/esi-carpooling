package com.esi.ridediscovery.dto;

import java.util.UUID;

public record RideRecommendationResponse(
        UUID recommendationId,
        String rideId,
        double relevanceScore,
        double distanceToOriginKm,
        double distanceToDestinationKm,
        double driverRating
) {
}
