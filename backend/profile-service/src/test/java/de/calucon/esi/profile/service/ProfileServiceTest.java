package de.calucon.esi.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.calucon.esi.profile.entity.UserProfile;
import de.calucon.esi.profile.repository.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void handleValidationSuccess_UpdatesToVerified() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.PENDING)
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act: Simulation of Validation Success event [cite: 296]
        profileService.handleValidationSuccess(userId);

        // Assert
        assertEquals(UserProfile.DriverStatus.VERIFIED, profile.getDriverStatus());
        assertFalse(profile.getAccountFlagged());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void handleValidationFailure_FlagsAccount() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.PENDING)
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act: Simulation of Validation Failure event [cite: 297, 299]
        profileService.handleValidationFailure(userId, "Documents illegible");

        // Assert
        assertEquals(UserProfile.DriverStatus.REJECTED, profile.getDriverStatus());
        assertTrue(profile.getAccountFlagged()); // Verification of "flagged account" requirement [cite: 299]
    }
}