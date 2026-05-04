package com.esi.validation.dto;

import java.util.UUID;

public class DocumentDTO {
    private UUID documentId;
    private String documentType;
    private String fileName;
    private String contentType;

    public DocumentDTO() {
    }

    public DocumentDTO(UUID documentId, String documentType) {
        this.documentId = documentId;
        this.documentType = documentType;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
