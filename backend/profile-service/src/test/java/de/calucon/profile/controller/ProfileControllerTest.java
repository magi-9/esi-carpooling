package de.calucon.profile.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.esi.profile.controller.ProfileController;
import de.calucon.esi.profile.dto.CreateProfileRequest;
import de.calucon.esi.profile.dto.CreateVehicleRequest;
import de.calucon.esi.profile.dto.UpdateDriverStatusRequest;
import de.calucon.esi.profile.dto.UpdateProfileRequest;
import de.calucon.esi.profile.dto.UserProfileResponse;
import de.calucon.esi.profile.dto.VehicleResponse;
import de.calucon.esi.profile.entity.UserProfile;
import de.calucon.esi.profile.exception.DuplicateProfileException;
import de.calucon.esi.profile.exception.GlobalExceptionHandler;
import de.calucon.esi.profile.exception.ProfileNotFoundException;
import de.calucon.esi.profile.service.ProfileService;

@DisplayName("ProfileController Unit Tests")
class ProfileControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private ProfileService profileService;

        @InjectMocks
        private ProfileController profileController;

        private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        private static final UUID TEST_VEHICLE_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
                objectMapper = new ObjectMapper();
        }

        // ==================== POST /profiles Tests ====================

        @Nested
        @DisplayName("POST /profiles - Create Profile")
        class CreateProfileTests {

                @Test
                @DisplayName("Should create profile successfully with valid request")
                void createProfile_Success() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .driverStatus(UserProfile.DriverStatus.NONE)
                                        .build();

                        when(profileService.createProfile(any(UUID.class), any(CreateProfileRequest.class)))
                                        .thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()))
                                        .andExpect(jsonPath("$.firstName").value("John"))
                                        .andExpect(jsonPath("$.lastName").value("Doe"))
                                        .andExpect(jsonPath("$.driverStatus").value("NONE"));

                        verify(profileService, times(1)).createProfile(any(UUID.class),
                                        any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when first name is missing")
                void createProfile_MissingFirstName_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.firstName").exists());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when last name is missing")
                void createProfile_MissingLastName_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.lastName").exists());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when first name exceeds max length")
                void createProfile_FirstNameTooLong_ReturnsBadRequest() throws Exception {
                        // Arrange
                        String longFirstName = "A".repeat(51);
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName(longFirstName)
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.firstName").exists());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when first name contains invalid characters")
                void createProfile_InvalidFirstName_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John123")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.firstName").exists());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when phone number is invalid")
                void createProfile_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("invalid")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.phoneNumber").exists());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 409 Conflict when profile already exists")
                void createProfile_DuplicateProfile_ReturnsConflict() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        when(profileService.createProfile(any(UUID.class), any(CreateProfileRequest.class)))
                                        .thenThrow(new DuplicateProfileException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value("Profile already exists"));

                        verify(profileService, times(1)).createProfile(any(UUID.class),
                                        any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when request body is empty")
                void createProfile_EmptyBody_ReturnsBadRequest() throws Exception {
                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .header("X-User-Id", TEST_USER_ID.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isBadRequest());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 Bad Request when X-User-Id header is missing")
                void createProfile_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateProfileRequest request = CreateProfileRequest.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        verify(profileService, never()).createProfile(any(UUID.class), any(CreateProfileRequest.class));
                }
        }

        // ==================== GET /profiles/{userId} Tests ====================

        @Nested
        @DisplayName("GET /profiles/{userId} - Get Profile")
        class GetProfileTests {

                @Test
                @DisplayName("Should return profile successfully")
                void getProfile_Success() throws Exception {
                        // Arrange
                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .driverStatus(UserProfile.DriverStatus.VERIFIED)
                                        .build();

                        when(profileService.getProfile(TEST_USER_ID)).thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()))
                                        .andExpect(jsonPath("$.firstName").value("John"))
                                        .andExpect(jsonPath("$.lastName").value("Doe"))
                                        .andExpect(jsonPath("$.driverStatus").value("VERIFIED"));

                        verify(profileService, times(1)).getProfile(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void getProfile_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        when(profileService.getProfile(TEST_USER_ID))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}", TEST_USER_ID))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).getProfile(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return 400 for invalid UUID format")
                void getProfile_InvalidUUID_ReturnsBadRequest() throws Exception {
                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}", "invalid-uuid"))
                                        .andExpect(status().isBadRequest());

                        verify(profileService, never()).getProfile(any(UUID.class));
                }
        }

        // ==================== PUT /profiles/{userId} Tests ====================

        @Nested
        @DisplayName("PUT /profiles/{userId} - Update Profile")
        class UpdateProfileTests {

                @Test
                @DisplayName("Should update profile successfully")
                void updateProfile_Success() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .firstName("Jane")
                                        .phoneNumber("+49 987 654321")
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("Jane")
                                        .lastName("Doe")
                                        .phoneNumber("+49 987 654321")
                                        .driverStatus(UserProfile.DriverStatus.VERIFIED)
                                        .build();

                        when(profileService.updateProfile(TEST_USER_ID, request)).thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.firstName").value("Jane"))
                                        .andExpect(jsonPath("$.phoneNumber").value("+49 987 654321"));

                        verify(profileService, times(1)).updateProfile(TEST_USER_ID, request);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void updateProfile_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .firstName("Jane")
                                        .build();

                        when(profileService.updateProfile(TEST_USER_ID, request))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).updateProfile(TEST_USER_ID, request);
                }

                @Test
                @DisplayName("Should return 400 when first name is invalid")
                void updateProfile_InvalidFirstName_ReturnsBadRequest() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .firstName("J@ne")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        verify(profileService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when phone number is invalid")
                void updateProfile_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .phoneNumber("invalid")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        verify(profileService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
                }

                @Test
                @DisplayName("Should allow partial update with only firstName")
                void updateProfile_PartialUpdate_FirstNameOnly_Success() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .firstName("Jane")
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("Jane")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .driverStatus(UserProfile.DriverStatus.NONE)
                                        .build();

                        when(profileService.updateProfile(TEST_USER_ID, request)).thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.firstName").value("Jane"));

                        verify(profileService, times(1)).updateProfile(TEST_USER_ID, request);
                }

                @Test
                @DisplayName("Should allow partial update with only phoneNumber")
                void updateProfile_PartialUpdate_PhoneNumberOnly_Success() throws Exception {
                        // Arrange
                        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                        .phoneNumber("+49 987 654321")
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 987 654321")
                                        .driverStatus(UserProfile.DriverStatus.NONE)
                                        .build();

                        when(profileService.updateProfile(TEST_USER_ID, request)).thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.phoneNumber").value("+49 987 654321"));

                        verify(profileService, times(1)).updateProfile(TEST_USER_ID, request);
                }
        }

        // ==================== GET /profiles/{userId}/vehicles Tests
        // ====================

        @Nested
        @DisplayName("GET /profiles/{userId}/vehicles - Get Vehicles")
        class GetVehiclesTests {

                @Test
                @DisplayName("Should return list of vehicles successfully")
                void getVehicles_Success() throws Exception {
                        // Arrange
                        VehicleResponse vehicle = VehicleResponse.builder()
                                        .vehicleId(TEST_VEHICLE_ID)
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .isVerified(true)
                                        .build();

                        when(profileService.getVehicles(TEST_USER_ID)).thenReturn(Arrays.asList(vehicle));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0].vehicleId").value(TEST_VEHICLE_ID.toString()))
                                        .andExpect(jsonPath("$[0].make").value("Toyota"))
                                        .andExpect(jsonPath("$[0].isVerified").value(true));

                        verify(profileService, times(1)).getVehicles(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return empty list when user has no vehicles")
                void getVehicles_EmptyList_Success() throws Exception {
                        // Arrange
                        when(profileService.getVehicles(TEST_USER_ID)).thenReturn(Collections.emptyList());

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$").isEmpty());

                        verify(profileService, times(1)).getVehicles(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void getVehicles_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        when(profileService.getVehicles(TEST_USER_ID))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles", TEST_USER_ID))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).getVehicles(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return multiple vehicles correctly")
                void getVehicles_MultipleVehicles_Success() throws Exception {
                        // Arrange
                        VehicleResponse vehicle1 = VehicleResponse.builder()
                                        .vehicleId(TEST_VEHICLE_ID)
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .isVerified(true)
                                        .build();

                        UUID vehicle2Id = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
                        VehicleResponse vehicle2 = VehicleResponse.builder()
                                        .vehicleId(vehicle2Id)
                                        .make("Honda")
                                        .model("Civic")
                                        .licensePlate("B-CD 5678")
                                        .isVerified(false)
                                        .build();

                        when(profileService.getVehicles(TEST_USER_ID)).thenReturn(Arrays.asList(vehicle1, vehicle2));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.length()").value(2))
                                        .andExpect(jsonPath("$[0].make").value("Toyota"))
                                        .andExpect(jsonPath("$[1].make").value("Honda"));

                        verify(profileService, times(1)).getVehicles(TEST_USER_ID);
                }
        }

        // ==================== GET /profiles/{userId}/vehicles/verified Tests
        // ====================

        @Nested
        @DisplayName("GET /profiles/{userId}/vehicles/verified - Get Verified Vehicles")
        class GetVerifiedVehiclesTests {

                @Test
                @DisplayName("Should return list of verified vehicles successfully")
                void getVerifiedVehicles_Success() throws Exception {
                        // Arrange
                        VehicleResponse vehicle = VehicleResponse.builder()
                                        .vehicleId(TEST_VEHICLE_ID)
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .isVerified(true)
                                        .build();

                        when(profileService.getVerifiedVehicles(TEST_USER_ID)).thenReturn(Arrays.asList(vehicle));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles/verified", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0].vehicleId").value(TEST_VEHICLE_ID.toString()))
                                        .andExpect(jsonPath("$[0].isVerified").value(true));

                        verify(profileService, times(1)).getVerifiedVehicles(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return empty list when user has no verified vehicles")
                void getVerifiedVehicles_EmptyList_Success() throws Exception {
                        // Arrange
                        when(profileService.getVerifiedVehicles(TEST_USER_ID)).thenReturn(Collections.emptyList());

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles/verified", TEST_USER_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$").isEmpty());

                        verify(profileService, times(1)).getVerifiedVehicles(TEST_USER_ID);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void getVerifiedVehicles_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        when(profileService.getVerifiedVehicles(TEST_USER_ID))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(get("/profiles/{userId}/vehicles/verified", TEST_USER_ID))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).getVerifiedVehicles(TEST_USER_ID);
                }
        }

        // ==================== POST /profiles/{userId}/vehicles Tests
        // ====================

        @Nested
        @DisplayName("POST /profiles/{userId}/vehicles - Add Vehicle")
        class AddVehicleTests {

                @Test
                @DisplayName("Should add vehicle successfully")
                void addVehicle_Success() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .build();

                        VehicleResponse response = VehicleResponse.builder()
                                        .vehicleId(TEST_VEHICLE_ID)
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .isVerified(false)
                                        .build();

                        when(profileService.addVehicle(TEST_USER_ID, request)).thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.vehicleId").value(TEST_VEHICLE_ID.toString()))
                                        .andExpect(jsonPath("$.make").value("Toyota"))
                                        .andExpect(jsonPath("$.isVerified").value(false));

                        verify(profileService, times(1)).addVehicle(TEST_USER_ID, request);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void addVehicle_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .build();

                        when(profileService.addVehicle(TEST_USER_ID, request))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).addVehicle(TEST_USER_ID, request);
                }

                @Test
                @DisplayName("Should return 400 when make is missing")
                void addVehicle_MissingMake_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.make").exists());

                        verify(profileService, never()).addVehicle(any(UUID.class), any(CreateVehicleRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when model is missing")
                void addVehicle_MissingModel_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make("Toyota")
                                        .licensePlate("B-AB 1234")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.model").exists());

                        verify(profileService, never()).addVehicle(any(UUID.class), any(CreateVehicleRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when license plate is missing")
                void addVehicle_MissingLicensePlate_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make("Toyota")
                                        .model("Camry")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.licensePlate").exists());

                        verify(profileService, never()).addVehicle(any(UUID.class), any(CreateVehicleRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when license plate format is invalid")
                void addVehicle_InvalidLicensePlate_ReturnsBadRequest() throws Exception {
                        // Arrange
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make("Toyota")
                                        .model("Camry")
                                        .licensePlate("INVALID")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.licensePlate").exists());

                        verify(profileService, never()).addVehicle(any(UUID.class), any(CreateVehicleRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when make exceeds max length")
                void addVehicle_MakeTooLong_ReturnsBadRequest() throws Exception {
                        // Arrange
                        String longMake = "A".repeat(51);
                        CreateVehicleRequest request = CreateVehicleRequest.builder()
                                        .make(longMake)
                                        .model("Camry")
                                        .licensePlate("B-AB 1234")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post("/profiles/{userId}/vehicles", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.make").exists());

                        verify(profileService, never()).addVehicle(any(UUID.class), any(CreateVehicleRequest.class));
                }
        }

        // ==================== PUT /profiles/{userId}/status Tests ====================

        @Nested
        @DisplayName("PUT /profiles/{userId}/status - Update Driver Status")
        class UpdateDriverStatusTests {

                @Test
                @DisplayName("Should update driver status to PENDING successfully")
                void updateDriverStatus_ToPending_Success() throws Exception {
                        // Arrange
                        UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                                        .driverStatus(UserProfile.DriverStatus.PENDING)
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .driverStatus(UserProfile.DriverStatus.PENDING)
                                        .build();

                        when(profileService.updateDriverStatus(TEST_USER_ID, UserProfile.DriverStatus.PENDING))
                                        .thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}/status", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.driverStatus").value("PENDING"));

                        verify(profileService, times(1)).updateDriverStatus(TEST_USER_ID,
                                        UserProfile.DriverStatus.PENDING);
                }

                @Test
                @DisplayName("Should update driver status to VERIFIED successfully")
                void updateDriverStatus_ToVerified_Success() throws Exception {
                        // Arrange
                        UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                                        .driverStatus(UserProfile.DriverStatus.VERIFIED)
                                        .build();

                        UserProfileResponse response = UserProfileResponse.builder()
                                        .userId(TEST_USER_ID)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .phoneNumber("+49 123 456789")
                                        .driverStatus(UserProfile.DriverStatus.VERIFIED)
                                        .build();

                        when(profileService.updateDriverStatus(TEST_USER_ID, UserProfile.DriverStatus.VERIFIED))
                                        .thenReturn(response);

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}/status", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.driverStatus").value("VERIFIED"));

                        verify(profileService, times(1)).updateDriverStatus(TEST_USER_ID,
                                        UserProfile.DriverStatus.VERIFIED);
                }

                @Test
                @DisplayName("Should return 404 when profile not found")
                void updateDriverStatus_NotFound_ReturnsNotFound() throws Exception {
                        // Arrange
                        UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                                        .driverStatus(UserProfile.DriverStatus.PENDING)
                                        .build();

                        when(profileService.updateDriverStatus(TEST_USER_ID, UserProfile.DriverStatus.PENDING))
                                        .thenThrow(new ProfileNotFoundException(TEST_USER_ID));

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}/status", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Profile not found"));

                        verify(profileService, times(1)).updateDriverStatus(TEST_USER_ID,
                                        UserProfile.DriverStatus.PENDING);
                }

                @Test
                @DisplayName("Should return 400 when driver status is missing")
                void updateDriverStatus_MissingStatus_ReturnsBadRequest() throws Exception {
                        // Arrange
                        UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                                        .build();

                        // Act & Assert
                        mockMvc.perform(put("/profiles/{userId}/status", TEST_USER_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.errors.driverStatus").exists());

                        verify(profileService, never()).updateDriverStatus(any(UUID.class),
                                        any(UserProfile.DriverStatus.class));
                }

                @Test
                @DisplayName("Should handle all driver status values correctly")
                void allDriverStatusValues_Success() throws Exception {
                        UserProfile.DriverStatus[] statuses = {
                                        UserProfile.DriverStatus.NONE,
                                        UserProfile.DriverStatus.PENDING,
                                        UserProfile.DriverStatus.VERIFIED,
                                        UserProfile.DriverStatus.REJECTED
                        };

                        for (UserProfile.DriverStatus status : statuses) {
                                UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                                                .driverStatus(status)
                                                .build();

                                UserProfileResponse response = UserProfileResponse.builder()
                                                .userId(TEST_USER_ID)
                                                .firstName("John")
                                                .lastName("Doe")
                                                .driverStatus(status)
                                                .build();

                                when(profileService.updateDriverStatus(TEST_USER_ID, status)).thenReturn(response);

                                mockMvc.perform(put("/profiles/{userId}/status", TEST_USER_ID)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.driverStatus").value(status.name()));

                                verify(profileService, times(1)).updateDriverStatus(TEST_USER_ID, status);
                        }
                }
        }
}