package de.calucon.esi.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.calucon.esi.auth.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Spring Data JPA magically translates this method name into a SQL query:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}
