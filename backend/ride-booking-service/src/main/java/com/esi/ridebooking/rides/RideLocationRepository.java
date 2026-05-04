package com.esi.ridebooking.rides;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideLocationRepository extends JpaRepository<RideLocation, UUID> {

	// Derived query method: Spring Data JPA generates the query automatically
	List<RideLocation> findByDisplayAddress(String displayAddress);

	// Example of a custom JPQL query
	// @Query("SELECT r FROM RideLocation r WHERE r.latitude = :lat AND r.longitude
	// = :lon")
	// RideLocation findByCoordinates(Double lat, Double lon);
}
