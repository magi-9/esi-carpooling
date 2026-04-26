package de.calucon.profile.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.calucon.profile.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    @Query("SELECT v FROM Vehicle v WHERE v.userProfile.userId = :userId AND v.isVerified = true")
    List<Vehicle> findVerifiedVehiclesByUserId(@Param("userId") UUID userId);

    List<Vehicle> findByUserProfileUserId(UUID userId);
}