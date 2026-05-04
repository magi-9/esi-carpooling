package de.calucon.esi.profile.dto;

import de.calucon.esi.profile.entity.UserProfile;
import jakarta.validation.constraints.NotNull;
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
public class UpdateDriverStatusRequest {

    @NotNull(message = "Driver status is required")
    private UserProfile.DriverStatus driverStatus;
}