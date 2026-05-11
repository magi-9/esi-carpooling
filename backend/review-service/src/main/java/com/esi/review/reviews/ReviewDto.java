package com.esi.review.reviews;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewDto {
    
    private UUID reviewId;
    private UUID bookingId;
    private UUID rideId;
    private UUID reviewerId;
    private String comment;
    private Integer stars;
    private LocalDateTime createdAt;

    public static ReviewDto fromEntity(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setBookingId(review.getBookingId());
        dto.setRideId(review.getRideId());
        dto.setReviewerId(review.getReviewerId());
        dto.setComment(review.getComment());
        dto.setStars(review.getStars());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getRideId() {
        return rideId;
    }

    public void setRideId(UUID rideId) {
        this.rideId = rideId;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(UUID reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
