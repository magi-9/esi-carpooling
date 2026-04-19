package com.esi.ridediscovery.domain;

/**
 * Value object representing a geographic location.
 */
public record Location(
        double latitude,
        double longitude,
        String displayAddress
) {
}
