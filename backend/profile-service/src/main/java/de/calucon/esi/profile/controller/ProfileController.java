package de.calucon.esi.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.calucon.esi.profile.dto.CreateProfileRequest;
import de.calucon.esi.profile.dto.CreateVehicleRequest;
import de.calucon.esi.profile.dto.UpdateDriverStatusRequest;
import de.calucon.esi.profile.dto.UpdateProfileRequest;
import de.calucon.esi.profile.dto.UserProfileResponse;
import de.calucon.esi.profile.dto.VehicleResponse;
import de.calucon.esi.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Service", description = "User profile and vehicle management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @PreAuthorize("#request.userId.toString() == authentication.name")
    @Operation(summary = "Create a new user profile", description = "Create a new user profile (triggered after successful auth registration)")
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        // userId is now extracted from the request body
        UserProfileResponse response = profileService.createProfile(request.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Retrieve user profile data")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId) {
        UserProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Update basic profile information", description = "Update basic profile information (name, phone number)")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/vehicles")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Retrieve all vehicles associated with a driver")
    public ResponseEntity<List<VehicleResponse>> getVehicles(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId) {
        List<VehicleResponse> vehicles = profileService.getVehicles(userId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{userId}/vehicles/verified")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Retrieve all verified vehicles associated with a driver")
    public ResponseEntity<List<VehicleResponse>> getVerifiedVehicles(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId) {
        List<VehicleResponse> vehicles = profileService.getVerifiedVehicles(userId);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/{userId}/vehicles")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Add a new vehicle to the driver's profile")
    public ResponseEntity<VehicleResponse> addVehicle(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId,
            @Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = profileService.addVehicle(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("#userId.toString() == authentication.name")
    @Operation(summary = "Update driver's verification status", description = "Update a driver's verification status (e.g., from pending to verified)")
    public ResponseEntity<UserProfileResponse> updateDriverStatus(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID userId,
            @Valid @RequestBody UpdateDriverStatusRequest request) {
        UserProfileResponse response = profileService.updateDriverStatus(userId, request.getDriverStatus());
        return ResponseEntity.ok(response);
    }
}