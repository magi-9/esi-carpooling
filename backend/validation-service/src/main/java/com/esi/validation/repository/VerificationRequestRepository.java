package com.esi.validation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esi.validation.model.VerificationRequest;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    List<VerificationRequest> findByUserId(UUID userId);
    List<VerificationRequest> findByVehicleId(UUID vehicleId);
}
