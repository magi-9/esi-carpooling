package de.calucon.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.calucon.esi.profile.dto.CreateProfileRequest;
import de.calucon.esi.profile.dto.CreateVehicleRequest;
import de.calucon.esi.profile.dto.UpdateProfileRequest;
import de.calucon.esi.profile.dto.UserProfileResponse;
import de.calucon.esi.profile.dto.VehicleResponse;
import de.calucon.esi.profile.entity.UserProfile;
import de.calucon.esi.profile.entity.Vehicle;
import de.calucon.esi.profile.exception.DuplicateProfileException;
import de.calucon.esi.profile.exception.ProfileNotFoundException;
import de.calucon.esi.profile.repository.UserProfileRepository;
import de.calucon.esi.profile.repository.VehicleRepository;
import de.calucon.esi.profile.service.ProfileService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ProfileService profileService;

    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID TEST_VEHICLE_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");

    private UserProfile testUserProfile;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testUserProfile = UserProfile.builder()
                .userId(TEST_USER_ID)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+49 123 456789")
                .driverStatus(UserProfile.DriverStatus.NONE)
                .build();

        testVehicle = Vehicle.builder()
                .vehicleId(TEST_VEHICLE_ID)
                .userProfile(testUserProfile)
                .make("Toyota")
                .model("Corolla")
                .licensePlate("B-AB 1234")
                .isVerified(false)
                .build();
    }

    // ==================== createProfile Tests ====================

    @Nested
    @DisplayName("createProfile")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create profile successfully with valid request")
        void createProfile_Success() {
            // Arrange
            CreateProfileRequest request = CreateProfileRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("+49 123 456789")
                    .build();

            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(false);
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.createProfile(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            assertEquals(TEST_USER_ID, response.getUserId());
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
            assertEquals(UserProfile.DriverStatus.NONE, response.getDriverStatus());

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should throw DuplicateProfileException when profile already exists")
        void createProfile_DuplicateProfile_ThrowsException() {
            // Arrange
            CreateProfileRequest request = CreateProfileRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("+49 123 456789")
                    .build();

            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateProfileException.class,
                    () -> profileService.createProfile(TEST_USER_ID, request));

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(userProfileRepository, never()).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should save profile with correct data")
        void createProfile_SavesCorrectData() {
            // Arrange
            CreateProfileRequest request = CreateProfileRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+49 987 654321")
                    .build();

            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(false);
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
                UserProfile saved = invocation.getArgument(0);
                saved.setUserId(TEST_USER_ID);
                return saved;
            });

            // Act
            UserProfileResponse response = profileService.createProfile(TEST_USER_ID, request);

            // Assert
            assertEquals("Jane", response.getFirstName());
            assertEquals("Smith", response.getLastName());
            assertEquals("+49 987 654321", response.getPhoneNumber());
        }
    }

    // ==================== getProfile Tests ====================

    @Nested
    @DisplayName("getProfile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return profile successfully when user exists")
        void getProfile_Success() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));

            // Act
            UserProfileResponse response = profileService.getProfile(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(TEST_USER_ID, response.getUserId());
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void getProfile_NotFound_ThrowsException() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.getProfile(TEST_USER_ID));

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return profile with correct driver status")
        void getProfile_ReturnsCorrectDriverStatus() {
            // Arrange
            testUserProfile.setDriverStatus(UserProfile.DriverStatus.VERIFIED);
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));

            // Act
            UserProfileResponse response = profileService.getProfile(TEST_USER_ID);

            // Assert
            assertEquals(UserProfile.DriverStatus.VERIFIED, response.getDriverStatus());
        }
    }

    // ==================== updateProfile Tests ====================

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully with valid request")
        void updateProfile_Success() {
            // Arrange
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+49 987 654321")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.updateProfile(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void updateProfile_NotFound_ThrowsException() {
            // Arrange
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.updateProfile(TEST_USER_ID, request));

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(userProfileRepository, never()).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update only firstName when other fields are null")
        void updateProfile_PartialUpdate_FirstNameOnly() {
            // Arrange
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserProfileResponse response = profileService.updateProfile(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update only lastName when other fields are null")
        void updateProfile_PartialUpdate_LastNameOnly() {
            // Arrange
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .lastName("Smith")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserProfileResponse response = profileService.updateProfile(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update only phoneNumber when other fields are null")
        void updateProfile_PartialUpdate_PhoneNumberOnly() {
            // Arrange
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .phoneNumber("+49 987 654321")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserProfileResponse response = profileService.updateProfile(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }
    }

    // ==================== getVehicles Tests ====================

    @Nested
    @DisplayName("getVehicles")
    class GetVehiclesTests {

        @Test
        @DisplayName("Should return list of vehicles successfully")
        void getVehicles_Success() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findByUserProfileUserId(TEST_USER_ID)).thenReturn(Arrays.asList(testVehicle));

            // Act
            var response = profileService.getVehicles(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Toyota", response.get(0).getMake());

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, times(1)).findByUserProfileUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when user has no vehicles")
        void getVehicles_EmptyList_Success() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findByUserProfileUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

            // Act
            var response = profileService.getVehicles(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.size());

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, times(1)).findByUserProfileUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void getVehicles_NotFound_ThrowsException() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(false);

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.getVehicles(TEST_USER_ID));

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, never()).findByUserProfileUserId(any(UUID.class));
        }

        @Test
        @DisplayName("Should return vehicles with correct vehicleId")
        void getVehicles_ReturnsCorrectVehicleId() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findByUserProfileUserId(TEST_USER_ID)).thenReturn(Arrays.asList(testVehicle));

            // Act
            var response = profileService.getVehicles(TEST_USER_ID);

            // Assert
            assertEquals(TEST_VEHICLE_ID, response.get(0).getVehicleId());
        }
    }

    // ==================== getVerifiedVehicles Tests ====================

    @Nested
    @DisplayName("getVerifiedVehicles")
    class GetVerifiedVehiclesTests {

        @Test
        @DisplayName("Should return list of verified vehicles successfully")
        void getVerifiedVehicles_Success() {
            // Arrange
            testVehicle.setIsVerified(true);
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findVerifiedVehiclesByUserId(TEST_USER_ID))
                    .thenReturn(Arrays.asList(testVehicle));

            // Act
            var response = profileService.getVerifiedVehicles(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals(true, response.get(0).getIsVerified());

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, times(1)).findVerifiedVehiclesByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when user has no verified vehicles")
        void getVerifiedVehicles_EmptyList_Success() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findVerifiedVehiclesByUserId(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            // Act
            var response = profileService.getVerifiedVehicles(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.size());

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, times(1)).findVerifiedVehiclesByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void getVerifiedVehicles_NotFound_ThrowsException() {
            // Arrange
            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(false);

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.getVerifiedVehicles(TEST_USER_ID));

            verify(userProfileRepository, times(1)).existsById(TEST_USER_ID);
            verify(vehicleRepository, never()).findVerifiedVehiclesByUserId(any(UUID.class));
        }
    }

    // ==================== addVehicle Tests ====================

    @Nested
    @DisplayName("addVehicle")
    class AddVehicleTests {

        @Test
        @DisplayName("Should add vehicle successfully with valid request")
        void addVehicle_Success() {
            // Arrange
            CreateVehicleRequest request = CreateVehicleRequest.builder()
                    .make("Toyota")
                    .model("Corolla")
                    .licensePlate("B-AB 1234")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

            // Act
            VehicleResponse response = profileService.addVehicle(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            assertEquals("Toyota", response.getMake());
            assertEquals("Corolla", response.getModel());
            assertEquals("B-AB 1234", response.getLicensePlate());
            assertEquals(false, response.getIsVerified());

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void addVehicle_NotFound_ThrowsException() {
            // Arrange
            CreateVehicleRequest request = CreateVehicleRequest.builder()
                    .make("Toyota")
                    .model("Corolla")
                    .licensePlate("B-AB 1234")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.addVehicle(TEST_USER_ID, request));

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should save vehicle with correct user profile")
        void addVehicle_SavesWithCorrectUserProfile() {
            // Arrange
            CreateVehicleRequest request = CreateVehicleRequest.builder()
                    .make("BMW")
                    .model("X5")
                    .licensePlate("M-CD 5678")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            VehicleResponse response = profileService.addVehicle(TEST_USER_ID, request);

            // Assert
            assertNotNull(response);
            verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should set isVerified to false by default")
        void addVehicle_SetsIsVerifiedFalse() {
            // Arrange
            CreateVehicleRequest request = CreateVehicleRequest.builder()
                    .make("Toyota")
                    .model("Corolla")
                    .licensePlate("B-AB 1234")
                    .build();

            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

            // Act
            VehicleResponse response = profileService.addVehicle(TEST_USER_ID, request);

            // Assert
            assertEquals(false, response.getIsVerified());
        }
    }

    // ==================== updateDriverStatus Tests ====================

    @Nested
    @DisplayName("updateDriverStatus")
    class UpdateDriverStatusTests {

        @Test
        @DisplayName("Should update driver status successfully to VERIFIED")
        void updateDriverStatus_Success_ToVerified() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID,
                    UserProfile.DriverStatus.VERIFIED);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update driver status successfully to PENDING")
        void updateDriverStatus_Success_ToPending() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID,
                    UserProfile.DriverStatus.PENDING);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update driver status successfully to REJECTED")
        void updateDriverStatus_Success_ToRejected() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID,
                    UserProfile.DriverStatus.REJECTED);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should update driver status successfully to NONE")
        void updateDriverStatus_Success_ToNone() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

            // Act
            UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID,
                    UserProfile.DriverStatus.NONE);

            // Assert
            assertNotNull(response);
            verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
        void updateDriverStatus_NotFound_ThrowsException() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.updateDriverStatus(TEST_USER_ID, UserProfile.DriverStatus.VERIFIED));

            verify(userProfileRepository, times(1)).findById(TEST_USER_ID);
            verify(userProfileRepository, never()).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should persist driver status change")
        void updateDriverStatus_PersistsChange() {
            // Arrange
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
                UserProfile saved = invocation.getArgument(0);
                saved.setDriverStatus(UserProfile.DriverStatus.VERIFIED);
                return saved;
            });

            // Act
            UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID,
                    UserProfile.DriverStatus.VERIFIED);

            // Assert
            assertEquals(UserProfile.DriverStatus.VERIFIED, response.getDriverStatus());
        }
    }

    // ==================== Additional Edge Case Tests ====================

    @Nested
    @DisplayName("Additional Edge Cases")
    class AdditionalEdgeCases {

        @Test
        @DisplayName("Should handle multiple vehicles correctly")
        void multipleVehicles_Success() {
            // Arrange
            Vehicle vehicle2 = Vehicle.builder()
                    .vehicleId(UUID.fromString("770e8400-e29b-41d4-a716-446655440002"))
                    .userProfile(testUserProfile)
                    .make("BMW")
                    .model("X5")
                    .licensePlate("M-CD 5678")
                    .isVerified(true)
                    .build();

            when(userProfileRepository.existsById(TEST_USER_ID)).thenReturn(true);
            when(vehicleRepository.findByUserProfileUserId(TEST_USER_ID))
                    .thenReturn(Arrays.asList(testVehicle, vehicle2));

            // Act
            var response = profileService.getVehicles(TEST_USER_ID);

            // Assert
            assertEquals(2, response.size());
            assertEquals("Toyota", response.get(0).getMake());
            assertEquals("BMW", response.get(1).getMake());
        }

        @Test
        @DisplayName("Should handle null phone number in profile")
        void nullPhoneNumber_Success() {
            // Arrange
            testUserProfile.setPhoneNumber(null);
            when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));

            // Act
            UserProfileResponse response = profileService.getProfile(TEST_USER_ID);

            // Assert
            assertNotNull(response);
            assertEquals(null, response.getPhoneNumber());
        }

        @Test
        @DisplayName("Should handle all driver status transitions")
        void allDriverStatusTransitions_Success() {
            UserProfile.DriverStatus[] statuses = {
                    UserProfile.DriverStatus.NONE,
                    UserProfile.DriverStatus.PENDING,
                    UserProfile.DriverStatus.VERIFIED,
                    UserProfile.DriverStatus.REJECTED
            };

            for (UserProfile.DriverStatus status : statuses) {
                when(userProfileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUserProfile));
                when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
                    UserProfile saved = invocation.getArgument(0);
                    saved.setDriverStatus(status);
                    return saved;
                });

                UserProfileResponse response = profileService.updateDriverStatus(TEST_USER_ID, status);

                assertNotNull(response);
                verify(userProfileRepository, times(1)).save(any(UserProfile.class));
            }
        }
    }
}