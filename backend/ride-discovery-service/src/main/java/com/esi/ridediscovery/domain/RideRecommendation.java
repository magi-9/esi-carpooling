package com.esi.ridediscovery.domain;

import java.util.UUID;

/**
 * Entity representing a ranked ride recommendation.
 */
public class RideRecommendation {

    private final UUID recommendationId;
    private final String rideId;
    private final double relevanceScore;
    private final double distanceToOriginKm;
    private final double distanceToDestinationKm;
    private final double driverRating;

    private RideRecommendation(
            UUID recommendationId,
            String rideId,
            double relevanceScore,
            double distanceToOriginKm,
            double distanceToDestinationKm,
            double driverRating) {
        this.recommendationId = recommendationId;
        this.rideId = rideId;
        this.relevanceScore = relevanceScore;
        this.distanceToOriginKm = distanceToOriginKm;
        this.distanceToDestinationKm = distanceToDestinationKm;
        this.driverRating = driverRating;
    }

    public static RideRecommendation of(
            String rideId,
            double relevanceScore,
            double distanceToOriginKm,
            double distanceToDestinationKm,
            double driverRating) {
        return new RideRecommendation(
                UUID.randomUUID(),
                rideId,
                relevanceScore,
                distanceToOriginKm,
                distanceToDestinationKm,
                driverRating);
    }

    public UUID getRecommendationId() {
        return recommendationId;
    }

    public String getRideId() {
        return rideId;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public double getDistanceToOriginKm() {
        return distanceToOriginKm;
    }

    public double getDistanceToDestinationKm() {
        return distanceToDestinationKm;
    }

    public double getDriverRating() {
        return driverRating;
    }
}
