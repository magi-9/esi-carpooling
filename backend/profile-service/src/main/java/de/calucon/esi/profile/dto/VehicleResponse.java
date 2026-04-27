package de.calucon.esi.profile.dto;

import java.util.UUID;

import de.calucon.esi.profile.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private UUID vehicleId;
    private UUID userId;
    private String make;
    private String model;
    private String licensePlate;
    private Boolean isVerified;

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .userId(vehicle.getUserProfile().getUserId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .licensePlate(vehicle.getLicensePlate())
                .isVerified(vehicle.getIsVerified())
                .build();
    }
}