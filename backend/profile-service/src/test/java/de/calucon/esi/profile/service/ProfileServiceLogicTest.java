package de.calucon.esi.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import de.calucon.esi.profile.exception.ProfileNotFoundException;
import de.calucon.esi.profile.repository.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceLogicTest {

    @Mock
    private UserProfileRepository repository;
    @InjectMocks
    private ProfileService profileService;

    @Test
    void createInitialProfile_IsIdempotent() {
        UUID userId = UUID.randomUUID();
        when(repository.existsById(userId)).thenReturn(true);

        profileService.createInitialProfile(userId);

        // Should check existence but never call save if it exists
        verify(repository, never()).save(any());
    }

    @Test
    void handleValidationFailure_CorrectlyFlagsAccount() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.PENDING)
                .build();
        when(repository.findById(userId)).thenReturn(Optional.of(profile));

        profileService.handleValidationFailure(userId, "Expired License");

        // Assert
        assertEquals(UserProfile.DriverStatus.REJECTED, profile.getDriverStatus());
        assertTrue(profile.getAccountFlagged()); // "Flags the account" requirement [cite: 299]
        verify(repository).save(profile);
    }

    @Test
    void getProfile_ThrowsException_WhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfile(userId));
    }
}