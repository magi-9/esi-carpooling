package com.esi.ridediscovery.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Value object encapsulating search criteria for ride discovery.
 */
public record SearchCriteria(
        Location origin,
        Location destination,
        LocalDate departureDate,
        int seatsNeeded,
        BigDecimal maxPricePerSeat
) {
}
