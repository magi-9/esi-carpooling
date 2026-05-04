package com.esi.validation.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.esi.validation.dto.CreateVerificationRequestDTO;
import com.esi.validation.dto.VerificationRequestDTO;
import com.esi.validation.service.ValidationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/validation")
public class ValidationController {

    private final ValidationService service;

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VerificationRequestDTO> create(
            @RequestPart("data") @Valid CreateVerificationRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        VerificationRequestDTO created = service.createVerification(dto, files);
        URI location = URI.create(String.format("/validation/%s", created.getRequestId()));
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{requestId}/retry")
    public ResponseEntity<VerificationRequestDTO> retry(@PathVariable UUID requestId) {
        VerificationRequestDTO updated = service.retry(requestId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<VerificationRequestDTO> getByRequestId(@PathVariable UUID requestId) {
        return ResponseEntity.ok(service.getById(requestId));
    }

    @GetMapping
    public ResponseEntity<List<VerificationRequestDTO>> getByQuery(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID vehicleId) {

        if (userId != null && vehicleId == null) {
            return ResponseEntity.ok(service.getByUserId(userId));
        }

        if (vehicleId != null && userId == null) {
            return ResponseEntity.ok(service.getByVehicleId(vehicleId));
        }

        if (userId != null && vehicleId != null) {
            List<VerificationRequestDTO> filtered = service.getByUserId(userId).stream()
                    .filter(v -> vehicleId.equals(v.getVehicleId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        }

        return ResponseEntity.badRequest().build();
    }
}
