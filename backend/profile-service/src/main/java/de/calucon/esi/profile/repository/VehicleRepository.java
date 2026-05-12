package de.calucon.esi.profile.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.calucon.esi.profile.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByUserProfileUserIdAndVerificationStatus(UUID userId,
            Vehicle.VerificationStatus verificationStatus);

    List<Vehicle> findByUserProfileUserId(UUID userId);
}
