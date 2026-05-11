package com.esi.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClient;

import com.esi.review.client.RideBookingServiceClient.BookingDto;
import com.esi.review.reviews.Review;
import com.esi.review.reviews.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestClient.Builder restClientBuilder;

    private static final UUID TEST_BOOKING_ID = UUID.fromString("880e8400-e29b-41d4-a716-446655440001");
    private static final UUID TEST_REVIEWER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    // Test JWT token with reviewerId in sub claim
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDIiLCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJyb2xlcyI6WyJQQVNTRU5HRVIiXSwiaWF0IjoxNzA0MDY3MjAwLCJleHAiOjE3MDY2NTkyMDB9.test";

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        // Create mock RestClient
        RestClient mockClient = createMockRestClient();
        
        // Mock the builder: builder.baseUrl(any).build() -> mockClient
        // baseUrl() should return the same builder for chaining
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(mockClient);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RestClient createMockRestClient() {
        RestClient mockClient = org.mockito.Mockito.mock(RestClient.class);
        
        // Mock GET requests for auth validation
        RestClient.RequestHeadersUriSpec getSpec = org.mockito.Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec getResponse = org.mockito.Mockito.mock(RestClient.ResponseSpec.class);
        
        when(mockClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString(), org.mockito.ArgumentMatchers.<Object>any())).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getSpec);
        when(getSpec.header(anyString(), anyString())).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(getResponse);
        when(getResponse.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        when(getResponse.body(BookingDto.class)).thenAnswer(invocation -> {
            BookingDto dto = new BookingDto();
            dto.setBookingId(TEST_BOOKING_ID);
            dto.setRideId(UUID.randomUUID());
            dto.setPassengerId(TEST_REVIEWER_ID);
            dto.setStatus("COMPLETED");
            return dto;
        });
        
        return mockClient;
    }

    @Test
    void testCreateReview_Success() throws Exception {
        String requestJson = """
            {
                "bookingId": "880e8400-e29b-41d4-a716-446655440001",
                "comment": "Great ride, very comfortable!",
                "stars": 5
            }
            """;

        MvcResult result = mockMvc.perform(post("/reviews")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        UUID reviewId = UUID.fromString(result.getResponse().getContentAsString().replace("\"", ""));
        
        // Verify review was created
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        assertEquals(TEST_BOOKING_ID, review.getBookingId());
        assertEquals("Great ride, very comfortable!", review.getComment());
        assertEquals(5, review.getStars());
        assertFalse(review.getDeleted());
    }

    @Test
    void testCreateReview_DuplicatePrevention() throws Exception {
        // Create first review
        testCreateReview_Success();

        // Try to create duplicate
        String requestJson = """
            {
                "bookingId": "880e8400-e29b-41d4-a716-446655440001",
                "comment": "Another review",
                "stars": 4
            }
            """;

        mockMvc.perform(post("/reviews")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void testCreateReview_InvalidStars() throws Exception {
        String requestJson = """
            {
                "bookingId": "880e8400-e29b-41d4-a716-446655440001",
                "comment": "Test",
                "stars": 6
            }
            """;

        mockMvc.perform(post("/reviews")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReview_NotFound_Returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/reviews/{reviewId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Review not found")));
    }

    @Test
    void testDeleteReview_SoftDelete() throws Exception {
        // Create a review first
        testCreateReview_Success();
        
        Review review = reviewRepository.findAll().get(0);
        UUID reviewId = review.getReviewId();

        // Delete it
        mockMvc.perform(delete("/reviews/{reviewId}", reviewId)
                .header("Authorization", "Bearer " + TEST_TOKEN))
                .andExpect(status().isNoContent());

        // Verify soft delete - review should still exist but marked as deleted
        Review deletedReview = reviewRepository.findById(reviewId).orElseThrow();
        assertTrue(deletedReview.getDeleted());

        // GET should now return 404
        mockMvc.perform(get("/reviews/{reviewId}", reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReview_OnlyReviewer() throws Exception {
        // Create a review
        testCreateReview_Success();
        Review review = reviewRepository.findAll().get(0);

        String updateJson = """
            {
                "comment": "Updated comment",
                "stars": 3
            }
            """;

        mockMvc.perform(put("/reviews/{reviewId}", review.getReviewId())
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertEquals("Updated comment", updated.getComment());
        assertEquals(3, updated.getStars());
    }
}
