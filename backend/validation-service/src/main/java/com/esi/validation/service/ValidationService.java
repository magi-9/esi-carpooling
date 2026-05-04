package com.esi.validation.service;

import java.util.List;
import java.util.UUID;

import com.esi.validation.dto.CreateVerificationRequestDTO;
import com.esi.validation.dto.VerificationRequestDTO;

public interface ValidationService {
    VerificationRequestDTO createVerification(CreateVerificationRequestDTO dto, java.util.List<org.springframework.web.multipart.MultipartFile> files);
    VerificationRequestDTO retry(UUID requestId);
    VerificationRequestDTO getById(UUID requestId);
    List<VerificationRequestDTO> getByUserId(UUID userId);
    List<VerificationRequestDTO> getByVehicleId(UUID vehicleId);
}
