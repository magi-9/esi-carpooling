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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(request, authHeader);
        return review.getReviewId();
    }

    @GetMapping
    public List<ReviewDto> getAllReviews(
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Integer maxStars) {
        return reviewService.getAllReviews(bookingId, reviewerId, minStars, maxStars);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable UUID reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ReviewDto.fromEntity(review));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReviewDto dto) {
        Review updatedReview = reviewService.updateReview(reviewId, dto, authHeader);
        return ResponseEntity.ok(ReviewDto.fromEntity(updatedReview));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        reviewService.deleteReview(reviewId, authHeader);
        return ResponseEntity.noContent().build();
    }
}
