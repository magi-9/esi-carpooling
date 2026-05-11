package com.esi.review.reviews;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Review management for completed rides")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review", description = "Create a review for a completed booking")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input (e.g., stars out of range)"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public UUID createReview(
            @Parameter(description = "JWT Authorization header") @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(request, authHeader);
        return review.getReviewId();
    }

    @GetMapping
    @Operation(summary = "List reviews", description = "Get all reviews with optional filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of reviews")
    })
    public List<ReviewDto> getAllReviews(
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) UUID bookingId,
            @Parameter(description = "Filter by reviewer ID") @RequestParam(required = false) UUID reviewerId,
            @Parameter(description = "Minimum stars filter") @RequestParam(required = false) Integer minStars,
            @Parameter(description = "Maximum stars filter") @RequestParam(required = false) Integer maxStars) {
        return reviewService.getAllReviews(bookingId, reviewerId, minStars, maxStars);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Get a single review by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review found"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewDto> getReview(
            @Parameter(description = "Review ID") @PathVariable UUID reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ReviewDto.fromEntity(review));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update a review", description = "Update an existing review (owner only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review updated"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Not the review owner"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewDto> updateReview(
            @Parameter(description = "Review ID") @PathVariable UUID reviewId,
            @Parameter(description = "JWT Authorization header") @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReviewDto dto) {
        Review updatedReview = reviewService.updateReview(reviewId, dto, authHeader);
        return ResponseEntity.ok(ReviewDto.fromEntity(updatedReview));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review", description = "Soft delete a review (owner only)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Review deleted"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Not the review owner"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable UUID reviewId,
            @Parameter(description = "JWT Authorization header") @RequestHeader(value = "Authorization", required = false) String authHeader) {
        reviewService.deleteReview(reviewId, authHeader);
        return ResponseEntity.noContent().build();
    }
}
