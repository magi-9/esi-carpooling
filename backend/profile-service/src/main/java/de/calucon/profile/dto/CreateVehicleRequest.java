package de.calucon.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CreateVehicleRequest {

    @NotBlank(message = "Make is required")
    @Size(min = 1, max = 50, message = "Make must be between 1 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-']+$", message = "Make contains invalid characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-']+$", message = "Model contains invalid characters")
    private String model;

    @NotBlank(message = "License plate is required")
    @Size(min = 2, max = 15, message = "License plate must be between 2 and 15 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-]+$", message = "License plate contains invalid characters")
    private String licensePlate;
}