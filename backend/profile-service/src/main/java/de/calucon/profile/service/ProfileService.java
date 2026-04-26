package de.calucon.profile.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.calucon.profile.dto.CreateProfileRequest;
import de.calucon.profile.dto.CreateVehicleRequest;
import de.calucon.profile.dto.UpdateProfileRequest;
import de.calucon.profile.dto.UserProfileResponse;
import de.calucon.profile.dto.VehicleResponse;
import de.calucon.profile.entity.UserProfile;
import de.calucon.profile.entity.Vehicle;
import de.calucon.profile.exception.DuplicateProfileException;
import de.calucon.profile.exception.ProfileNotFoundException;
import de.calucon.profile.repository.UserProfileRepository;
import de.calucon.profile.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public UserProfileResponse createProfile(UUID userId, CreateProfileRequest request) {
        if (userProfileRepository.existsById(userId)) {
            throw new DuplicateProfileException(userId);
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .driverStatus(UserProfile.DriverStatus.NONE)
                .build();

        profile = userProfileRepository.save(profile);
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        profile = userProfileRepository.save(profile);
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles(UUID userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new ProfileNotFoundException(userId);
        }
        return vehicleRepository.findByUserProfileUserId(userId).stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVerifiedVehicles(UUID userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new ProfileNotFoundException(userId);
        }
        return vehicleRepository.findVerifiedVehiclesByUserId(userId).stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse addVehicle(UUID userId, CreateVehicleRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        Vehicle vehicle = Vehicle.builder()
                .userProfile(profile)
                .make(request.getMake())
                .model(request.getModel())
                .licensePlate(request.getLicensePlate())
                .isVerified(false)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional
    public UserProfileResponse updateDriverStatus(UUID userId, UserProfile.DriverStatus status) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        profile.setDriverStatus(status);
        profile = userProfileRepository.save(profile);
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional
    public void updateDriverStatusFromValidation(UUID userId, boolean isApproved) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        profile.setDriverStatus(isApproved ? UserProfile.DriverStatus.VERIFIED : UserProfile.DriverStatus.REJECTED);
        userProfileRepository.save(profile);
    }
}