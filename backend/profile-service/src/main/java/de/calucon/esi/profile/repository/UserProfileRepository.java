package de.calucon.esi.profile.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.calucon.esi.profile.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
