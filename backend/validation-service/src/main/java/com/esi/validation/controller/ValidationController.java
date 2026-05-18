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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VerificationRequestDTO> create(
            @RequestPart("data") @Valid CreateVerificationRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt) {

        // Ensure the request is associated with the authenticated user (prevent spoofing)
        dto.setUserId(UUID.fromString(jwt.getSubject()));

        VerificationRequestDTO created = service.createVerification(dto, files);
        URI location = URI.create(String.format("/validation/%s", created.getRequestId()));
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{requestId}/retry")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VerificationRequestDTO> retry(@PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {
        // Only the owner of the verification request may retry it
        UUID currentUser = UUID.fromString(jwt.getSubject());
        VerificationRequestDTO existing = service.getById(requestId);
        if (!existing.getUserId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to retry this request");
        }

        VerificationRequestDTO updated = service.retry(requestId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<VerificationRequestDTO> getByRequestId(@PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUser = UUID.fromString(jwt.getSubject());
        VerificationRequestDTO dto = service.getById(requestId);
        if (!dto.getUserId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this request");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<VerificationRequestDTO>> getByQuery(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID vehicleId,
            @AuthenticationPrincipal Jwt jwt) {

        // If querying by userId ensure the caller is that user
        if (userId != null) {
            UUID currentUser = UUID.fromString(jwt.getSubject());
            if (!userId.equals(currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to query other users' data");
            }
        }

        if (userId != null && vehicleId == null) {
            return ResponseEntity.ok(service.getByUserId(userId));
        }

        if (vehicleId != null && userId == null) {
            UUID currentUser = UUID.fromString(jwt.getSubject());
            List<VerificationRequestDTO> list = service.getByVehicleId(vehicleId).stream()
                    .filter(v -> v.getUserId().equals(currentUser))
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to query this vehicle");
            }
            return ResponseEntity.ok(list);
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
