package de.calucon.esi.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.calucon.esi.profile.client.ValidationClient;
import de.calucon.esi.profile.dto.CreateVehicleRequest;
import de.calucon.esi.profile.entity.UserProfile;
import de.calucon.esi.profile.entity.Vehicle;
import de.calucon.esi.profile.repository.UserProfileRepository;
import de.calucon.esi.profile.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ValidationClient validationClient;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void addVehicle_RequestsVehicleValidation() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        UserProfile profile = UserProfile.builder().userId(userId).build();
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setVehicleId(vehicleId);
            return vehicle;
        });

        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Toyota")
                .model("Corolla")
                .licensePlate("ABC123")
                .build();

        profileService.addVehicle(userId, request);

        verify(validationClient).requestVehicleVerification(eq(userId), eq(vehicleId));
    }

    @Test
    void updateDriverStatus_PendingRequestsDriverValidation() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.NONE).build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profileService.updateDriverStatus(userId, UserProfile.DriverStatus.PENDING);

        assertEquals(UserProfile.DriverStatus.PENDING, profile.getDriverStatus());
        verify(validationClient).requestDriverVerification(userId);
        verify(userProfileRepository).save(profile);
    }

    @Test
    void handleValidationSuccess_UpdatesToVerified() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.PENDING).build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        profileService.handleValidationSuccess(userId);

        assertEquals(UserProfile.DriverStatus.VERIFIED, profile.getDriverStatus());
        assertFalse(profile.getAccountFlagged());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void handleValidationFailure_FlagsAccount() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(userId).driverStatus(UserProfile.DriverStatus.PENDING).build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        profileService.handleValidationFailure(userId, "Documents illegible");

        assertEquals(UserProfile.DriverStatus.REJECTED, profile.getDriverStatus());
        assertTrue(profile.getAccountFlagged());
    }

    @Test
    void handleVehicleValidationSuccess_MarksVehicleVerified() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder().vehicleId(vehicleId).isVerified(false).build();

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        profileService.handleVehicleValidationSuccess(vehicleId);

        assertTrue(vehicle.getIsVerified());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void handleVehicleValidationFailure_DoesNotThrowWhenMissing() {
        UUID vehicleId = UUID.randomUUID();

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        profileService.handleVehicleValidationFailure(vehicleId, "document expired");

        verify(vehicleRepository).findById(vehicleId);
    }
}
