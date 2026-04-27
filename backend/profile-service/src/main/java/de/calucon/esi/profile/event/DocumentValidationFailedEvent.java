package de.calucon.esi.profile.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentValidationFailedEvent {
    private UUID userId;
    private UUID vehicleId;
    private String reason; // Includes the reason for failure (e.g., "expired" or "illegible")
}