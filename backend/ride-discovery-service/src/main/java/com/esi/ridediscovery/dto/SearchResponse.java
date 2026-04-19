package com.esi.ridediscovery.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SearchResponse(
        UUID searchId,
        String passengerId,
        String status,
        Instant createdAt,
        SearchCriteriaResponse criteria,
        List<RideRecommendationResponse> recommendations
) {
}
