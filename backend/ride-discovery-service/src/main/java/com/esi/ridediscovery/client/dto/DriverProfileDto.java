package com.esi.ridediscovery.client.dto;

public record DriverProfileDto(
        String userId,
        String displayName,
        String avatarUrl
) {
}
