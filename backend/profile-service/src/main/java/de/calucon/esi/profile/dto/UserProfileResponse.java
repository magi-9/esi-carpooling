package de.calucon.esi.profile.dto;

import java.util.UUID;

import de.calucon.esi.profile.entity.UserProfile;
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
public class UserProfileResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserProfile.DriverStatus driverStatus;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .driverStatus(profile.getDriverStatus())
                .build();
    }
}