package com.esi.ridediscovery.client.dto;

import java.math.BigDecimal;

public record RideDto(
        String rideId,
        String driverId,
        double originLat,
        double originLon,
        String originAddress,
        double destinationLat,
        double destinationLon,
        String destinationAddress,
        String departureTime,
        BigDecimal pricePerSeat,
        int availableSeats
) {
}
