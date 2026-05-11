package com.esi.review;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.esi.review.client.AuthServiceClient;
import com.esi.review.client.RideBookingServiceClient;
import com.esi.review.reviews.Review;
import com.esi.review.reviews.ReviewController;
import com.esi.review.reviews.ReviewService;
import com.esi.review.util.JwtService;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private AuthServiceClient authServiceClient;

    @MockBean
    private RideBookingServiceClient rideBookingServiceClient;

    @MockBean
    private JwtService jwtService;

    private static final UUID TEST_REVIEW_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000");
    private static final UUID TEST_BOOKING_ID = UUID.fromString("880e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_REVIEWER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    @Test
    void testCreateReview_Returns201() throws Exception {
        Review mockReview = new Review();
        mockReview.setReviewId(TEST_REVIEW_ID);
        
        when(reviewService.createReview(any(), anyString())).thenReturn(mockReview);

        String requestJson = """
            {
                "bookingId": "880e8400-e29b-41d4-a716-446655440001",
                "comment": "Great ride!",
                "stars": 5
            }
            """;

        mockMvc.perform(post("/reviews")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + TEST_REVIEW_ID + "\""));
    }

    @Test
    void testCreateReview_InvalidStars_Returns400() throws Exception {
        String requestJson = """
            {
                "bookingId": "880e8400-e29b-41d4-a716-446655440001",
                "comment": "Bad",
                "stars": 6
            }
            """;

        when(reviewService.createReview(any(), anyString()))
            .thenThrow(new IllegalArgumentException("Stars must be between 1 and 5"));

        mockMvc.perform(post("/reviews")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReview_Returns200() throws Exception {
        Review mockReview = new Review();
        mockReview.setReviewId(TEST_REVIEW_ID);
        mockReview.setBookingId(TEST_BOOKING_ID);
        mockReview.setReviewerId(TEST_REVIEWER_ID);
        mockReview.setComment("Great ride!");
        mockReview.setStars(5);

        when(reviewService.getReviewById(TEST_REVIEW_ID)).thenReturn(mockReview);

        mockMvc.perform(get("/reviews/{reviewId}", TEST_REVIEW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(TEST_REVIEW_ID.toString()))
                .andExpect(jsonPath("$.stars").value(5));
    }

    @Test
    void testGetReview_NotFound_Returns404() throws Exception {
        when(reviewService.getReviewById(any()))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Review not found"));

        mockMvc.perform(get("/reviews/{reviewId}", TEST_REVIEW_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReview_Returns200() throws Exception {
        Review updatedReview = new Review();
        updatedReview.setReviewId(TEST_REVIEW_ID);
        updatedReview.setStars(4);
        
        when(reviewService.updateReview(any(), any(), anyString())).thenReturn(updatedReview);

        String updateJson = """
            {
                "comment": "Updated comment",
                "stars": 4
            }
            """;

        mockMvc.perform(put("/reviews/{reviewId}", TEST_REVIEW_ID)
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteReview_Returns204() throws Exception {
        mockMvc.perform(delete("/reviews/{reviewId}", TEST_REVIEW_ID)
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }
}
