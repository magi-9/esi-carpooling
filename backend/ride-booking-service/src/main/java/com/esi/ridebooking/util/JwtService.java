package com.esi.ridebooking.util;

import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts the user ID (UUID) from the JWT token subject claim.
     * This only parses the token locally without verifying the signature.
     * The token should already be validated by the auth service.
     */
    public UUID extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Split the token into parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse JSON and extract "sub" field which contains the user ID
            JsonNode jsonNode = objectMapper.readTree(payload);
            String userId = jsonNode.get("sub").asText();
            
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("User ID not found in token");
            }
            
            return UUID.fromString(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token: " + e.getMessage());
        }
    }

    /**
     * Extracts the email from the JWT token claims.
     */
    public String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            return jsonNode.get("email").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token: " + e.getMessage());
        }
    }

    /**
     * Extracts the roles from the JWT token claims.
     * Note: With Spring Security OAuth2 Resource Server, the token is already validated.
     */
    public Set<String> extractRoles(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT token format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);

            JsonNode rolesNode = jsonNode.get("roles");
            if (rolesNode == null || !rolesNode.isArray()) {
                return Collections.emptySet();
            }

            Set<String> roles = new HashSet<>();
            for (JsonNode role : rolesNode) {
                if (role != null && !role.isNull()) {
                    roles.add(role.asText());
                }
            }
            return roles;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token: " + e.getMessage());
        }
    }
}
