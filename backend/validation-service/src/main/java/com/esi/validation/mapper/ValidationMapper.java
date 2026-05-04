package com.esi.validation.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.esi.validation.dto.CreateVerificationRequestDTO;
import com.esi.validation.dto.DocumentDTO;
import com.esi.validation.dto.VerificationRequestDTO;
import com.esi.validation.model.Document;
import com.esi.validation.model.VerificationRequest;

@Component
public class ValidationMapper {

    public VerificationRequest toEntity(CreateVerificationRequestDTO dto) {
        VerificationRequest r = new VerificationRequest();
        r.setUserId(dto.getUserId());
        r.setVehicleId(dto.getVehicleId());
        r.setStatus("PENDING");
        r.setIsApproved(Boolean.FALSE);

        if (dto.getDocuments() != null) {
            List<Document> docs = dto.getDocuments().stream().map(d -> {
                Document doc = new Document();
                doc.setDocumentType(d.getDocumentType());
                doc.setVerificationRequest(r);
                return doc;
            }).collect(Collectors.toList());
            r.setDocuments(docs);
        } else {
            r.setDocuments(Collections.emptyList());
        }

        return r;
    }

    public VerificationRequestDTO toDto(VerificationRequest entity) {
        if (entity == null) return null;
        VerificationRequestDTO dto = new VerificationRequestDTO();
        dto.setRequestId(entity.getRequestId());
        dto.setUserId(entity.getUserId());
        dto.setVehicleId(entity.getVehicleId());
        dto.setStatus(entity.getStatus());
        dto.setIsApproved(entity.getIsApproved());
        if (entity.getDocuments() != null) {
            List<DocumentDTO> docs = entity.getDocuments().stream().map(this::toDto).collect(Collectors.toList());
            dto.setDocuments(docs);
        }
        return dto;
    }

    public DocumentDTO toDto(Document d) {
        if (d == null) return null;
        DocumentDTO dto = new DocumentDTO();
        dto.setDocumentId(d.getDocumentId());
        dto.setDocumentType(d.getDocumentType());
        dto.setFileName(d.getFileName());
        dto.setContentType(d.getContentType());
        return dto;
    }
}
