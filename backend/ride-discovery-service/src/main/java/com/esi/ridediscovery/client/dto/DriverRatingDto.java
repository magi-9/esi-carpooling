package com.esi.ridediscovery.client.dto;

public record DriverRatingDto(
        String driverId,
        double averageRating,
        int totalReviews
) {
}
