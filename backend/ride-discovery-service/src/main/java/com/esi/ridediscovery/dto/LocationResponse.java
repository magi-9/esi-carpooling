package com.esi.ridediscovery.dto;

public record LocationResponse(
        double latitude,
        double longitude,
        String displayAddress
) {
}
