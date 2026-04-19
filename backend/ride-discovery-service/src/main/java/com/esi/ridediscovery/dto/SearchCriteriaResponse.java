package com.esi.ridediscovery.dto;

import java.math.BigDecimal;

public record SearchCriteriaResponse(
        LocationResponse origin,
        LocationResponse destination,
        String departureDate,
        int seatsNeeded,
        BigDecimal maxPricePerSeat
) {
}
