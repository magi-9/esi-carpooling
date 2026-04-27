package de.calucon.esi.profile.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.calucon.esi.profile.entity.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}