package com.esi.review.reviews;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateReviewRequest {
    
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    private String comment;

    @NotNull(message = "Stars rating is required")
    @Min(value = 1, message = "Stars must be between 1 and 5")
    @Max(value = 5, message = "Stars must be between 1 and 5")
    private Integer stars;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
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
}
