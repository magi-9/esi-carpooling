package com.esi.review.reviews;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByBookingId(UUID bookingId);
    boolean existsByBookingIdAndDeletedFalse(UUID bookingId);
}
