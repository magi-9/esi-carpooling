package com.esi.ridediscovery.dto;

import java.math.BigDecimal;

public record SearchRequest(
        Double originLat,
        Double originLon,
        String originAddress,
        Double destinationLat,
        Double destinationLon,
        String destinationAddress,
        String departureDate,
        int seatsNeeded,
        BigDecimal maxPricePerSeat
) {
    public SearchRequest {
        if (seatsNeeded <= 0) seatsNeeded = 1;
        if (originAddress == null) {
            originAddress = "";
        } else {
            originAddress = originAddress.trim();
        }
        if (destinationAddress == null) {
            destinationAddress = "";
        } else {
            destinationAddress = destinationAddress.trim();
        }
        if (departureDate != null) {
            departureDate = departureDate.trim();
        }
    }
}
