package com.esi.validation.dto;

public class CreateDocumentDTO {
    private String documentType;

    public CreateDocumentDTO() {
    }

    public CreateDocumentDTO(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
