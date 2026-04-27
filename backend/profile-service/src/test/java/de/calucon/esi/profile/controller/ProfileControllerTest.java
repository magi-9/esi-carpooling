package de.calucon.esi.profile.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.esi.profile.dto.CreateProfileRequest;
import de.calucon.esi.profile.dto.CreateVehicleRequest;
import de.calucon.esi.profile.dto.UpdateDriverStatusRequest;
import de.calucon.esi.profile.dto.UpdateProfileRequest;
import de.calucon.esi.profile.dto.UserProfileResponse;
import de.calucon.esi.profile.dto.VehicleResponse;
import de.calucon.esi.profile.entity.UserProfile;
import de.calucon.esi.profile.exception.GlobalExceptionHandler;
import de.calucon.esi.profile.exception.ProfileNotFoundException;
import de.calucon.esi.profile.service.ProfileService;

@SpringBootTest
@Import(GlobalExceptionHandler.class) // Ensure our custom error handler is loaded
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private UUID userId;
    private UserProfileResponse defaultResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        defaultResponse = UserProfileResponse.builder()
                .userId(userId)
                .firstName("Simon")
                .lastName("Schwitz")
                .phoneNumber("+49123456789")
                .driverStatus(UserProfile.DriverStatus.NONE)
                .build();
    }

    // --- Profile Lifecycle Tests ---

    @Test
    @DisplayName("POST /profiles - Success: Create profile with ID in body")
    void createProfile_Success() throws Exception {
        CreateProfileRequest req = new CreateProfileRequest(userId, "Simon", "Schwitz", "+49123");
        when(profileService.createProfile(eq(userId), any())).thenReturn(defaultResponse);

        mockMvc.perform(post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("POST /profiles - Failure: Invalid name (Regex Violation)")
    void createProfile_InvalidName() throws Exception {
        CreateProfileRequest req = new CreateProfileRequest(userId, "Simon123!", "Schwitz", "+49123");

        mockMvc.perform(post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    @DisplayName("GET /profiles/{userId} - Success")
    void getProfile_Success() throws Exception {
        when(profileService.getProfile(userId)).thenReturn(defaultResponse);

        mockMvc.perform(get("/profiles/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Simon"));
    }

    @Test
    @DisplayName("PUT /profiles/{userId} - Success")
    void updateProfile_Success() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest("NewName", null, null);
        UserProfileResponse updated = UserProfileResponse.builder().userId(userId).firstName("NewName").build();
        when(profileService.updateProfile(eq(userId), any())).thenReturn(updated);

        mockMvc.perform(put("/profiles/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NewName"));
    }

    // --- Vehicle Management Tests ---

    @Test
    @DisplayName("GET /profiles/{userId}/vehicles - Success: Returns All Vehicles")
    void getVehicles_ReturnsAll() throws Exception {
        VehicleResponse v1 = VehicleResponse.builder().make("Audi").isVerified(false).build();
        VehicleResponse v2 = VehicleResponse.builder().make("Tesla").isVerified(true).build();

        when(profileService.getVehicles(userId)).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/profiles/{userId}/vehicles", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].make").value("Audi"))
                .andExpect(jsonPath("$[0].isVerified").value(false));
    }

    @Test
    @DisplayName("GET /profiles/{userId}/vehicles/verified - Success: Returns Verified Only")
    void getVerifiedVehicles_Success() throws Exception {
        VehicleResponse verifiedVehicle = VehicleResponse.builder()
                .make("Tesla")
                .isVerified(true)
                .build();

        when(profileService.getVerifiedVehicles(userId)).thenReturn(List.of(verifiedVehicle));

        mockMvc.perform(get("/profiles/{userId}/vehicles/verified", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isVerified").value(true))
                .andExpect(jsonPath("$[0].make").value("Tesla"));
    }

    @Test
    @DisplayName("POST /profiles/{userId}/vehicles - Success: Add new vehicle")
    void addVehicle_Success() throws Exception {
        CreateVehicleRequest req = new CreateVehicleRequest("BMW", "i3", "ABC-1234");
        VehicleResponse res = VehicleResponse.builder().make("BMW").isVerified(false).build();

        when(profileService.addVehicle(eq(userId), any())).thenReturn(res);

        mockMvc.perform(post("/profiles/{userId}/vehicles", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("BMW"));
    }

    // --- Status & Verification Tests ---

    @Test
    @DisplayName("PUT /profiles/{userId}/status - Success")
    void updateStatus_Success() throws Exception {
        UpdateDriverStatusRequest req = new UpdateDriverStatusRequest(UserProfile.DriverStatus.VERIFIED);
        UserProfileResponse res = UserProfileResponse.builder().driverStatus(UserProfile.DriverStatus.VERIFIED)
                .build();

        when(profileService.updateDriverStatus(eq(userId), any())).thenReturn(res);

        mockMvc.perform(put("/profiles/{userId}/status", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("PUT /profiles/{userId}/status - Failure: Profile Not Found (404)")
    void updateStatus_NotFound() throws Exception {
        UpdateDriverStatusRequest req = new UpdateDriverStatusRequest(UserProfile.DriverStatus.REJECTED);
        when(profileService.updateDriverStatus(eq(userId), any()))
                .thenThrow(new ProfileNotFoundException(userId));

        mockMvc.perform(put("/profiles/{userId}/status", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
