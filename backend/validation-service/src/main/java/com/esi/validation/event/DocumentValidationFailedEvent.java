package com.esi.validation.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentValidationFailedEvent {
    private UUID userId;
    private UUID vehicleId;
    private String reason;

    public DocumentValidationFailedEvent() {}

    public DocumentValidationFailedEvent(UUID userId, UUID vehicleId, String reason) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
