package com.esi.ridebooking.rides;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {
	List<Ride> findByRideId(UUID rideId);
}
