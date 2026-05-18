package com.esi.ridediscovery.service;

import com.esi.ridediscovery.client.GeolocationClient;
import com.esi.ridediscovery.client.ReviewClient;
import com.esi.ridediscovery.client.RideBookingClient;
import com.esi.ridediscovery.client.dto.RideDto;
import com.esi.ridediscovery.domain.Location;
import com.esi.ridediscovery.domain.RideSearch;
import com.esi.ridediscovery.domain.SearchCriteria;
import com.esi.ridediscovery.domain.SearchStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

        @Mock private GeolocationClient geolocationClient;
    @Mock private RideBookingClient rideBookingClient;
    @Mock private ReviewClient reviewClient;

    @InjectMocks
    private SearchService searchService;

    private SearchCriteria buildCriteria() {
        return new SearchCriteria(
                new Location(59.4, 24.7, "Tallinn"),
                new Location(58.4, 26.7, "Tartu"),
                LocalDate.now().plusDays(1),
                1,
                BigDecimal.valueOf(30)
        );
    }

    @Test
    void search_withResults_returnsCompleted() {
        RideDto ride = new RideDto("ride-1", "driver-1", 59.38, 24.66,
                "Tallinn Center", 58.37, 26.72, "Tartu Station",
                "2026-05-01T10:00", BigDecimal.valueOf(15), 3);

        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any(), anyString())).thenReturn(List.of(ride));
        when(reviewClient.getDriverRating(anyString(), anyString())).thenReturn(4.2);

        RideSearch result = searchService.search(buildCriteria(), "passenger-1", "Bearer token");

        assertThat(result.getStatus()).isEqualTo(SearchStatus.COMPLETED);
        assertThat(result.getRecommendations()).hasSize(1);
        assertThat(result.getRecommendations().get(0).getDriverRating()).isEqualTo(4.2);
        verify(rideBookingClient).searchRides(
                eq("59.4"),
                eq("24.7"),
                eq("58.4"),
                eq("26.7"),
                anyString(),
                eq(1),
                eq(BigDecimal.valueOf(30)),
                eq("Bearer token")
        );
    }

    @Test
    void search_withNoRides_returnsCompletedEmpty() {
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                                anyString(), anyInt(), any(), any())).thenReturn(List.of());

        RideSearch result = searchService.search(buildCriteria(), "passenger-1", null);

        assertThat(result.getStatus()).isEqualTo(SearchStatus.COMPLETED);
        assertThat(result.getRecommendations()).isEmpty();
    }

    @Test
    void search_whenClientThrows_returnsFailed() {
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any(), any())).thenThrow(new RuntimeException("service down"));

        RideSearch result = searchService.search(buildCriteria(), "passenger-1", null);

        assertThat(result.getStatus()).isEqualTo(SearchStatus.FAILED);
    }

    @Test
    void search_multipleRides_sortedByRelevanceDescending() {
        RideDto cheapRide = new RideDto("ride-cheap", "driver-a", 59.4, 24.7, "A",
                58.4, 26.7, "B", "2026-05-01T10:00", BigDecimal.valueOf(5), 2);
        RideDto expensiveRide = new RideDto("ride-exp", "driver-b", 59.4, 24.7, "A",
                58.4, 26.7, "B", "2026-05-01T10:00", BigDecimal.valueOf(25), 1);

        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any(), anyString())).thenReturn(List.of(expensiveRide, cheapRide));
        when(reviewClient.getDriverRating("driver-a", "tok")).thenReturn(5.0);
        when(reviewClient.getDriverRating("driver-b", "tok")).thenReturn(2.0);

        RideSearch result = searchService.search(buildCriteria(), "p1", "tok");

        assertThat(result.getRecommendations()).hasSize(2);
        double first = result.getRecommendations().get(0).getRelevanceScore();
        double second = result.getRecommendations().get(1).getRelevanceScore();
        assertThat(first).isGreaterThanOrEqualTo(second);
    }

    @Test
    void search_withAddressOnly_usesGeolocationClient() {
        SearchCriteria addressOnlyCriteria = new SearchCriteria(
                new Location(Double.NaN, Double.NaN, "Tallinn"),
                new Location(Double.NaN, Double.NaN, "Tartu"),
                LocalDate.now().plusDays(1),
                1,
                BigDecimal.valueOf(30)
        );

        when(geolocationClient.geocode("Tallinn", "tok"))
                .thenReturn(new Location(59.4, 24.7, "Tallinn"));
        when(geolocationClient.geocode("Tartu", "tok"))
                .thenReturn(new Location(58.4, 26.7, "Tartu"));
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any(), anyString())).thenReturn(List.of());

        RideSearch result = searchService.search(addressOnlyCriteria, "p1", "tok");

        assertThat(result.getStatus()).isEqualTo(SearchStatus.COMPLETED);
        assertThat(result.getCriteria().origin().latitude()).isEqualTo(59.4);
        assertThat(result.getCriteria().destination().longitude()).isEqualTo(26.7);
    }
}
