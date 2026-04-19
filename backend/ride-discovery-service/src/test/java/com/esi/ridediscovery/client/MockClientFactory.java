package com.esi.ridediscovery.client;

import com.esi.ridediscovery.client.dto.RideDto;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Factory for creating mock client stubs for non-existing services.
 * 
 * This is used in tests to mock:
 * - Ride Booking Service (returns available rides)
 * - Review Service (returns driver ratings)
 * - Authentication Service (handled via headers)
 */
public class MockClientFactory {

    /**
     * Creates a mock RideBookingClient that returns typical test rides
     */
    public static RideBookingClient createMockRideBookingClient() {
        RideBookingClient client = Mockito.mock(RideBookingClient.class);
        
        Mockito.when(client.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of(
                new RideDto("ride-1", "driver-1", 59.4, 24.7, "Tallinn",
                        58.4, 26.7, "Tartu", "2026-05-01T10:00", BigDecimal.valueOf(20), 2),
                new RideDto("ride-2", "driver-2", 59.35, 24.65, "Tallinn Center",
                        58.45, 26.75, "Tartu Center", "2026-05-01T11:00", BigDecimal.valueOf(18), 1),
                new RideDto("ride-3", "driver-3", 59.45, 24.75, "Tallinn North",
                        58.35, 26.65, "Tartu South", "2026-05-01T09:30", BigDecimal.valueOf(25), 3)
        ));
        
        return client;
    }

    /**
     * Creates a mock RideBookingClient that returns no rides (empty search)
     */
    public static RideBookingClient createMockRideBookingClientEmpty() {
        RideBookingClient client = Mockito.mock(RideBookingClient.class);
        
        Mockito.when(client.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of());
        
        return client;
    }

    /**
     * Creates a mock ReviewClient that returns high ratings
     */
    public static ReviewClient createMockReviewClientHighRatings() {
        ReviewClient client = Mockito.mock(ReviewClient.class);
        
        Mockito.when(client.getDriverRating("driver-1", null)).thenReturn(4.8);
        Mockito.when(client.getDriverRating("driver-2", null)).thenReturn(4.5);
        Mockito.when(client.getDriverRating("driver-3", null)).thenReturn(4.2);
        Mockito.when(client.getDriverRating(anyString(), any())).thenReturn(4.0);
        
        return client;
    }

    /**
     * Creates a mock ReviewClient that returns low ratings
     */
    public static ReviewClient createMockReviewClientLowRatings() {
        ReviewClient client = Mockito.mock(ReviewClient.class);
        
        Mockito.when(client.getDriverRating("driver-1", null)).thenReturn(2.5);
        Mockito.when(client.getDriverRating("driver-2", null)).thenReturn(2.0);
        Mockito.when(client.getDriverRating("driver-3", null)).thenReturn(3.0);
        Mockito.when(client.getDriverRating(anyString(), any())).thenReturn(2.5);
        
        return client;
    }

    /**
     * Creates a mock ReviewClient that returns null ratings (service unavailable)
     */
    public static ReviewClient createMockReviewClientUnavailable() {
        ReviewClient client = Mockito.mock(ReviewClient.class);
        
        Mockito.when(client.getDriverRating(anyString(), any())).thenReturn(null);
        
        return client;
    }

}
