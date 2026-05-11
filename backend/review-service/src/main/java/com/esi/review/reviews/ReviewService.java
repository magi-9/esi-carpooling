package com.esi.review.reviews;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esi.review.client.RideBookingServiceClient;
import com.esi.review.client.RideBookingServiceClient.BookingDto;
import com.esi.review.util.JwtService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RideBookingServiceClient rideBookingServiceClient;

    @Autowired
    private JwtService jwtService;

    public Review createReview(CreateReviewRequest request, String authHeader) {
        // Token is already validated by Spring Security OAuth2 Resource Server
        // Extract reviewerId from JWT
        UUID reviewerId = jwtService.extractUserId(authHeader);

        // Validate stars range
        if (request.getStars() < 1 || request.getStars() > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        // Fetch booking from ride-booking-service
        BookingDto booking = rideBookingServiceClient.getBooking(request.getBookingId());
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found");
        }

        // Validate booking status is COMPLETED
        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking must be completed to leave a review");
        }

        // Validate booking belongs to the reviewer
        if (!booking.getPassengerId().equals(reviewerId)) {
            throw new IllegalArgumentException("You can only review your own bookings");
        }

        // Check for duplicate review
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new IllegalArgumentException("Review already exists for this booking");
        }

        // Create and save review
        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setReviewerId(reviewerId);
        review.setComment(request.getComment());
        review.setStars(request.getStars());

        return reviewRepository.save(review);
    }

    public Review getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .filter(r -> !r.getDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    public List<ReviewDto> getAllReviews(UUID bookingId, UUID reviewerId, Integer minStars, Integer maxStars) {
        return reviewRepository.findAll().stream()
                .filter(r -> !r.getDeleted())
                .filter(r -> bookingId == null || r.getBookingId().equals(bookingId))
                .filter(r -> reviewerId == null || r.getReviewerId().equals(reviewerId))
                .filter(r -> minStars == null || r.getStars() >= minStars)
                .filter(r -> maxStars == null || r.getStars() <= maxStars)
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Review updateReview(UUID reviewId, ReviewDto dto, String authHeader) {
        // Token is already validated by Spring Security OAuth2 Resource Server
        // Extract reviewerId from JWT
        UUID reviewerId = jwtService.extractUserId(authHeader);

        Review review = getReviewById(reviewId);

        // Only reviewer can update
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }

        // Update mutable fields only
        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }
        if (dto.getStars() != null) {
            if (dto.getStars() < 1 || dto.getStars() > 5) {
                throw new IllegalArgumentException("Stars must be between 1 and 5");
            }
            review.setStars(dto.getStars());
        }

        return reviewRepository.save(review);
    }

    public void deleteReview(UUID reviewId, String authHeader) {
        // Token is already validated by Spring Security OAuth2 Resource Server
        // Extract reviewerId from JWT
        UUID reviewerId = jwtService.extractUserId(authHeader);

        Review review = getReviewById(reviewId);

        // Only reviewer can delete
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        // Soft delete
        review.softDelete();
        reviewRepository.save(review);
    }
}
