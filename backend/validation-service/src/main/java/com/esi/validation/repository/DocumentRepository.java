package com.esi.validation.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esi.validation.model.Document;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

}
