package com.esi.validation.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentValidatedEvent {
    private UUID userId;
    private UUID vehicleId;

    public DocumentValidatedEvent() {}

    public DocumentValidatedEvent(UUID userId, UUID vehicleId) {
        this.userId = userId;
        this.vehicleId = vehicleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }
}
