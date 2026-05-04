package de.calucon.esi.profile.exception;

import java.util.UUID;

public class DuplicateProfileException extends RuntimeException {
    public DuplicateProfileException(UUID userId) {
        super("Profile already exists for user: " + userId);
    }
}