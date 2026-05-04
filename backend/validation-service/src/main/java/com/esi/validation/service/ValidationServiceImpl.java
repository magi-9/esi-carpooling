package com.esi.validation.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.esi.validation.dto.CreateVerificationRequestDTO;
import com.esi.validation.dto.VerificationRequestDTO;
import com.esi.validation.mapper.ValidationMapper;
import com.esi.validation.model.VerificationRequest;
import com.esi.validation.repository.VerificationRequestRepository;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final VerificationRequestRepository repository;
    private final ValidationMapper mapper;

    public ValidationServiceImpl(VerificationRequestRepository repository, ValidationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public VerificationRequestDTO createVerification(CreateVerificationRequestDTO dto, java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        VerificationRequest entity = mapper.toEntity(dto);
        // attach file data to documents (match by index)
        if (files != null && !files.isEmpty() && entity.getDocuments() != null) {
            int count = Math.min(files.size(), entity.getDocuments().size());
            for (int i = 0; i < count; i++) {
                org.springframework.web.multipart.MultipartFile f = files.get(i);
                com.esi.validation.model.Document d = entity.getDocuments().get(i);
                try {
                    d.setFileName(f.getOriginalFilename());
                    d.setContentType(f.getContentType());
                    d.setData(f.getBytes());
                } catch (java.io.IOException e) {
                    System.out.println("Failed to read uploaded file: " + f.getOriginalFilename());
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to read uploaded file", e);
                }
            }
        }

        VerificationRequest saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public VerificationRequestDTO retry(UUID requestId) {
        VerificationRequest v = repository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification request not found"));
        v.setStatus("PENDING");
        v.setIsApproved(Boolean.FALSE);
        VerificationRequest saved = repository.save(v);
        return mapper.toDto(saved);
    }

    @Override
    public VerificationRequestDTO getById(UUID requestId) {
        VerificationRequest v = repository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification request not found"));
        return mapper.toDto(v);
    }

    @Override
    public List<VerificationRequestDTO> getByUserId(UUID userId) {
        List<VerificationRequest> list = repository.findByUserId(userId);
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<VerificationRequestDTO> getByVehicleId(UUID vehicleId) {
        List<VerificationRequest> list = repository.findByVehicleId(vehicleId);
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
