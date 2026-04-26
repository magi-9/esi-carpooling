package de.calucon.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.calucon.profile.dto.CreateProfileRequest;
import de.calucon.profile.dto.CreateVehicleRequest;
import de.calucon.profile.dto.UpdateDriverStatusRequest;
import de.calucon.profile.dto.UpdateProfileRequest;
import de.calucon.profile.dto.UserProfileResponse;
import de.calucon.profile.dto.VehicleResponse;
import de.calucon.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Service", description = "User profile and vehicle management endpoints")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @Operation(summary = "Create a new user profile", description = "Create a new user profile (triggered after successful auth registration)")
    public ResponseEntity<UserProfileResponse> createProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateProfileRequest request) {
        UserProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Retrieve user profile data")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        UserProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update basic profile information", description = "Update basic profile information (name, phone number)")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/vehicles")
    @Operation(summary = "Retrieve all vehicles associated with a driver")
    public ResponseEntity<List<VehicleResponse>> getVehicles(@PathVariable UUID userId) {
        List<VehicleResponse> vehicles = profileService.getVehicles(userId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{userId}/vehicles/verified")
    @Operation(summary = "Retrieve all verified vehicles associated with a driver")
    public ResponseEntity<List<VehicleResponse>> getVerifiedVehicles(@PathVariable UUID userId) {
        List<VehicleResponse> vehicles = profileService.getVerifiedVehicles(userId);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/{userId}/vehicles")
    @Operation(summary = "Add a new vehicle to the driver's profile")
    public ResponseEntity<VehicleResponse> addVehicle(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = profileService.addVehicle(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update driver's verification status", description = "Update a driver's verification status (e.g., from pending to verified)")
    public ResponseEntity<UserProfileResponse> updateDriverStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateDriverStatusRequest request) {
        UserProfileResponse response = profileService.updateDriverStatus(userId, request.getDriverStatus());
        return ResponseEntity.ok(response);
    }
}