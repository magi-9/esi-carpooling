package com.esi.validation.dto;

import java.util.List;
import java.util.UUID;

public class CreateVerificationRequestDTO {
    private UUID userId;
    private UUID vehicleId;
    private List<CreateDocumentDTO> documents;

    public CreateVerificationRequestDTO() {
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

    public List<CreateDocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CreateDocumentDTO> documents) {
        this.documents = documents;
    }
}
