package com.esi.ridebooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

import com.esi.ridebooking.bookings.Booking;
import com.esi.ridebooking.bookings.BookingRepository;
import com.esi.ridebooking.rides.Ride;
import com.esi.ridebooking.rides.RideLocation;
import com.esi.ridebooking.rides.RideLocationRepository;
import com.esi.ridebooking.rides.RideRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class RideBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestClient.Builder restClientBuilder;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RideLocationRepository rideLocationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Test JWT token with userId: 550e8400-e29b-41d4-a716-446655440000
    // This is a valid JWT format with the UUID in the sub claim
    private static final String TEST_DRIVER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6ImRyaXZlckB0ZXN0LmNvbSIsInJvbGVzIjpbIkRSSVZFUiJdLCJpYXQiOjE3MDQwNjcyMDAsImV4cCI6MTcwNjY1OTIwMH0.test";
    
    private static final String TEST_PASSENGER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NjBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDEiLCJlbWFpbCI6InBhc3NlbmdlckB0ZXN0LmNvbSIsInJvbGVzIjpbIlBBU1NFTkdFUiJdLCJpYXQiOjE3MDQwNjcyMDAsImV4cCI6MTcwNjY1OTIwMH0.test";

    @BeforeEach
    void setUp() {
        // Clean up before each test
        bookingRepository.deleteAll();
        rideRepository.deleteAll();
        rideLocationRepository.deleteAll();

        // Mock RestClient.Builder to return a properly configured RestClient mock
        RestClient mockClient = createMockRestClient();
        when(restClientBuilder.build()).thenReturn(mockClient);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RestClient createMockRestClient() {
        RestClient mockClient = org.mockito.Mockito.mock(RestClient.class);
        
        // Mock GET requests
        RestClient.RequestHeadersUriSpec getSpec = org.mockito.Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec getResponse = org.mockito.Mockito.mock(RestClient.ResponseSpec.class);
        
        when(mockClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getSpec);
        when(getSpec.header(anyString(), anyString())).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(getResponse);
        when(getResponse.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        when(getResponse.body(Map.class)).thenReturn(Map.of(
            "latitude", 54.6872,
            "longitude", 25.2797
        ));
        
        // Mock POST requests (for payment service)
        RestClient.RequestBodyUriSpec postSpec = org.mockito.Mockito.mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec postResponse = org.mockito.Mockito.mock(RestClient.ResponseSpec.class);
        
        when(mockClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postSpec);
        when(postSpec.header(anyString(), anyString())).thenReturn(postSpec);
        when(postSpec.body(any(Object.class))).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(postResponse);
        when(postResponse.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        when(postResponse.body(Map.class)).thenReturn(Map.of("paymentId", UUID.randomUUID().toString()));
        
        return mockClient;
    }

    // ============================================
    // RIDE CREATION TESTS
    // ============================================

    @Test
    void testCreateRide_DriverIdExtractedFromJwt_NotFromRequest() throws Exception {
        // Create ride request WITHOUT driverId
        String rideJson = """
                {
                    "vehicleId": "550e8400-e29b-41d4-a716-446655440001",
                    "availableSeats": 4,
                    "rideStartDate": "2026-05-10T14:00:00",
                    "seatPriceAmount": 15.50,
                    "seatPriceCurrency": "EUR",
                    "startAddress": "Vilnius City Center",
                    "endAddress": "Kaunas City Center"
                }
                """;

        MvcResult result = mockMvc.perform(post("/rides")
                .header("Authorization", "Bearer " + TEST_DRIVER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rideJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract ride ID from response
        UUID rideId = UUID.fromString(result.getResponse().getContentAsString().replace("\"", ""));

        // Verify ride was created with driverId from JWT (550e8400-e29b-41d4-a716-446655440000)
        Ride createdRide = rideRepository.findById(rideId).orElseThrow();
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), createdRide.getDriverId());
        assertEquals(4, createdRide.getAvailableSeats());
        assertEquals("PENDING", createdRide.getStatus());
    }

    @Test
    void testCreateRide_LocationsCreated() throws Exception {
        String rideJson = """
                {
                    "vehicleId": "550e8400-e29b-41d4-a716-446655440001",
                    "availableSeats": 3,
                    "rideStartDate": "2026-05-10T10:00:00",
                    "seatPriceAmount": 20.00,
                    "seatPriceCurrency": "EUR",
                    "startAddress": "Vilnius Airport",
                    "endAddress": "Kaunas Airport"
                }
                """;

        mockMvc.perform(post("/rides")
                .header("Authorization", "Bearer " + TEST_DRIVER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rideJson))
                .andExpect(status().isCreated());

        // Verify locations were created
        long locationCount = rideLocationRepository.count();
        assertEquals(2, locationCount);

        // Verify location addresses
        RideLocation startLoc = rideLocationRepository.findAll().get(0);
        RideLocation endLoc = rideLocationRepository.findAll().get(1);
        assertEquals("Vilnius Airport", startLoc.getDisplayAddress());
        assertEquals("Kaunas Airport", endLoc.getDisplayAddress());
    }

    // ============================================
    // RIDE SEARCH/FILTER TESTS
    // ============================================

    @Test
    void testGetRides_FilterByStatus() throws Exception {
        // Create two rides with different statuses
        createTestRide("PENDING");
        createTestRide("COMPLETED");

        // Search for PENDING rides
        MvcResult result = mockMvc.perform(get("/rides")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rides = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, rides.size());
        assertEquals("PENDING", rides.get(0).get("status").asText());
    }

    @Test
    void testGetRides_FilterByMaxPrice() throws Exception {
        // Create rides with different prices
        createTestRideWithPrice(new BigDecimal("10.00"));
        createTestRideWithPrice(new BigDecimal("25.00"));
        createTestRideWithPrice(new BigDecimal("50.00"));

        // Search for rides under 20 EUR
        MvcResult result = mockMvc.perform(get("/rides")
                .param("maxPricePerSeat", "20.00"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rides = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, rides.size());
        assertEquals(10.00, rides.get(0).get("seatPriceAmount").asDouble(), 0.01);
    }

    @Test
    void testGetRides_FilterByDepartureDate() throws Exception {
        // Create rides at different times
        createTestRideWithDate(LocalDateTime.of(2026, 5, 10, 10, 0));
        createTestRideWithDate(LocalDateTime.of(2026, 5, 15, 14, 0));

        // Search for rides around May 10, 2026 10:00
        MvcResult result = mockMvc.perform(get("/rides")
                .param("departureDate", "2026-05-10T10:00:00"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rides = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, rides.size());
    }

    @Test
    void testGetRides_FilterBySeatsNeeded() throws Exception {
        // Create ride with 2 available seats
        Ride ride = createTestRide("PENDING");
        ride.setAvailableSeats(2);
        rideRepository.save(ride);

        // Create 2 confirmed bookings (full)
        createBookingForRide(ride, "CONFIRMED");
        createBookingForRide(ride, "CONFIRMED");

        // Search for rides with seats needed = 1
        // Should return empty because ride is fully booked
        MvcResult result = mockMvc.perform(get("/rides")
                .param("seatsNeeded", "1"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rides = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(0, rides.size());
    }

    @Test
    void testGetRides_FilterByLocationProximity() throws Exception {
        // Create ride with specific coordinates
        RideLocation startLoc = new RideLocation();
        startLoc.setLatitude(54.6872);
        startLoc.setLongitude(25.2797);
        startLoc.setDisplayAddress("Vilnius Center");
        rideLocationRepository.save(startLoc);

        Ride ride = createTestRide("PENDING");
        ride.setStartLocation(startLoc);
        rideRepository.save(ride);

        // Search for rides near Vilnius Center (54.6872, 25.2797) with 5km radius
        MvcResult result = mockMvc.perform(get("/rides")
                .param("originLat", "54.6872")
                .param("originLon", "25.2797")
                .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rides = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, rides.size());
    }

    // ============================================
    // RIDE UPDATE TESTS
    // ============================================

    @Test
    void testUpdateRide_OnlyMutableFieldsUpdated() throws Exception {
        // Create a ride
        Ride ride = createTestRide("PENDING");
        UUID originalDriverId = ride.getDriverId();
        UUID originalVehicleId = ride.getVehicleId();
        Integer originalCapacity = ride.getAvailableSeats();

        // Try to update all fields including immutable ones
        String updateJson = """
                {
                    "driverId": "11111111-1111-1111-1111-111111111111",
                    "vehicleId": "22222222-2222-2222-2222-222222222222",
                    "availableSeats": 10,
                    "rideStartDate": "2026-06-01T12:00:00",
                    "seatPriceAmount": 30.00,
                    "seatPriceCurrency": "USD",
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/rides/{rideId}", ride.getRideId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // Verify only mutable fields were updated
        Ride updatedRide = rideRepository.findById(ride.getRideId()).orElseThrow();
        assertEquals(originalDriverId, updatedRide.getDriverId()); // Immutable - unchanged
        assertEquals(originalVehicleId, updatedRide.getVehicleId()); // Immutable - unchanged
        assertEquals(originalCapacity, updatedRide.getAvailableSeats()); // Immutable - unchanged
        assertEquals(LocalDateTime.of(2026, 6, 1, 12, 0), updatedRide.getRideStartDate()); // Mutable - changed
        assertEquals(new BigDecimal("30.00"), updatedRide.getSeatPriceAmount()); // Mutable - changed
        assertEquals("USD", updatedRide.getSeatPriceCurrency()); // Mutable - changed
        assertEquals("COMPLETED", updatedRide.getStatus()); // Mutable - changed
    }

    // ============================================
    // RIDE DELETION TESTS
    // ============================================

    @Test
    void testDeleteRide_CancelsBookingsAndMarksRideCancelled() throws Exception {
        // Create a ride
        Ride ride = createTestRide("PENDING");
        
        // Create bookings for this ride
        Booking booking1 = createBookingForRide(ride, "PENDING");
        Booking booking2 = createBookingForRide(ride, "CONFIRMED");
        Booking booking3 = createBookingForRide(ride, "CANCELLED"); // Already cancelled

        // Delete (cancel) the ride
        mockMvc.perform(delete("/rides/{rideId}", ride.getRideId()))
                .andExpect(status().isNoContent());

        // Verify ride still exists but is marked as CANCELLED (soft delete)
        Ride updatedRide = rideRepository.findById(ride.getRideId()).orElseThrow();
        assertEquals("CANCELLED", updatedRide.getStatus());

        // Verify bookings still reference the ride and are marked as CANCELLED
        Booking updatedBooking1 = bookingRepository.findById(booking1.getBookingId()).orElseThrow();
        Booking updatedBooking2 = bookingRepository.findById(booking2.getBookingId()).orElseThrow();
        Booking updatedBooking3 = bookingRepository.findById(booking3.getBookingId()).orElseThrow();

        assertEquals("CANCELLED", updatedBooking1.getStatus());
        assertEquals("CANCELLED", updatedBooking2.getStatus());
        assertEquals("CANCELLED", updatedBooking3.getStatus());
        
        // Verify bookings still have ride reference (not orphaned)
        assertNotNull(updatedBooking1.getRide());
        assertNotNull(updatedBooking2.getRide());
        assertNotNull(updatedBooking3.getRide());
        assertEquals(ride.getRideId(), updatedBooking1.getRide().getRideId());
    }

    // ============================================
    // BOOKING CREATION TESTS
    // ============================================

    @Test
    void testCreateBooking_PassengerIdExtractedFromJwt() throws Exception {
        // Create a ride
        Ride ride = createTestRide("PENDING");
        ride.setAvailableSeats(2);
        rideRepository.save(ride);

        // Create booking - no passengerId in request
        String bookingJson = "{}";

        MvcResult result = mockMvc.perform(post("/rides/{rideId}/bookings", ride.getRideId())
                .header("Authorization", "Bearer " + TEST_PASSENGER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isCreated())
                .andReturn();

        UUID bookingId = UUID.fromString(result.getResponse().getContentAsString().replace("\"", ""));

        // Verify booking was created with passengerId from JWT (660e8400-e29b-41d4-a716-446655440001)
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(UUID.fromString("660e8400-e29b-41d4-a716-446655440001"), booking.getPassengerId());
    }

    @Test
    void testCreateBooking_DuplicateBookingPrevention() throws Exception {
        // Create a ride
        Ride ride = createTestRide("PENDING");
        ride.setAvailableSeats(2);
        rideRepository.save(ride);

        // Create first booking
        mockMvc.perform(post("/rides/{rideId}/bookings", ride.getRideId())
                .header("Authorization", "Bearer " + TEST_PASSENGER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());

        // Try to create second booking for same ride with same passenger
        mockMvc.perform(post("/rides/{rideId}/bookings", ride.getRideId())
                .header("Authorization", "Bearer " + TEST_PASSENGER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("already has a booking")));
    }

    @Test
    void testCreateBooking_NoSeatsAvailable() throws Exception {
        // Create a ride with 1 seat
        Ride ride = createTestRide("PENDING");
        ride.setAvailableSeats(1);
        rideRepository.save(ride);

        // Book the only seat
        createBookingForRide(ride, "CONFIRMED");

        // Try to book again - should fail
        mockMvc.perform(post("/rides/{rideId}/bookings", ride.getRideId())
                .header("Authorization", "Bearer " + TEST_PASSENGER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No seats available")));
    }

    // ============================================
    // 404 NOT FOUND TESTS
    // ============================================

    @Test
    void testGetRide_NotFound_Returns404() throws Exception {
        UUID nonExistentRideId = UUID.randomUUID();

        mockMvc.perform(get("/rides/{rideId}", nonExistentRideId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ride not found")));
    }

    @Test
    void testUpdateRide_NotFound_Returns404() throws Exception {
        UUID nonExistentRideId = UUID.randomUUID();

        String updateJson = """
                {
                    "rideStartDate": "2026-06-01T12:00:00",
                    "seatPriceAmount": 30.00,
                    "seatPriceCurrency": "USD",
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/rides/{rideId}", nonExistentRideId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ride not found")));
    }

    @Test
    void testDeleteRide_NotFound_Returns404() throws Exception {
        UUID nonExistentRideId = UUID.randomUUID();

        mockMvc.perform(delete("/rides/{rideId}", nonExistentRideId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ride not found")));
    }

    @Test
    void testCreateBooking_RideNotFound_Returns404() throws Exception {
        UUID nonExistentRideId = UUID.randomUUID();

        mockMvc.perform(post("/rides/{rideId}/bookings", nonExistentRideId)
                .header("Authorization", "Bearer " + TEST_PASSENGER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ride not found")));
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private Ride createTestRide(String status) {
        RideLocation startLoc = new RideLocation();
        startLoc.setLatitude(54.6872);
        startLoc.setLongitude(25.2797);
        startLoc.setDisplayAddress("Test Start Location");
        rideLocationRepository.save(startLoc);

        RideLocation endLoc = new RideLocation();
        endLoc.setLatitude(54.8969);
        endLoc.setLongitude(23.8927);
        endLoc.setDisplayAddress("Test End Location");
        rideLocationRepository.save(endLoc);

        Ride ride = new Ride();
        ride.setDriverId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        ride.setVehicleId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        ride.setAvailableSeats(4);
        ride.setRideStartDate(LocalDateTime.now().plusDays(1));
        ride.setSeatPriceAmount(new BigDecimal("15.00"));
        ride.setSeatPriceCurrency("EUR");
        ride.setStatus(status);
        ride.setStartLocation(startLoc);
        ride.setEndLocation(endLoc);

        return rideRepository.save(ride);
    }

    private Ride createTestRideWithPrice(BigDecimal price) {
        Ride ride = createTestRide("PENDING");
        ride.setSeatPriceAmount(price);
        return rideRepository.save(ride);
    }

    private Ride createTestRideWithDate(LocalDateTime date) {
        Ride ride = createTestRide("PENDING");
        ride.setRideStartDate(date);
        return rideRepository.save(ride);
    }

    private Booking createBookingForRide(Ride ride, String status) {
        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassengerId(UUID.fromString("660e8400-e29b-41d4-a716-446655440001"));
        booking.setStatus(status);
        if ("CONFIRMED".equals(status)) {
            booking.setPaymentId(UUID.randomUUID());
        }
        return bookingRepository.save(booking);
    }
}
