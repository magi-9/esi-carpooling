package com.esi.ridebooking.bookings;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByRideRideId(UUID rideId);
}
