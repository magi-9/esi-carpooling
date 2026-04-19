package com.esi.ridediscovery.controller;

import com.esi.ridediscovery.client.GeolocationClient;
import com.esi.ridediscovery.client.ReviewClient;
import com.esi.ridediscovery.client.RideBookingClient;
import com.esi.ridediscovery.client.dto.RideDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideBookingClient rideBookingClient;
    @MockBean
    private ReviewClient reviewClient;
    @MockBean
        private GeolocationClient geolocationClient;

    @Test
    void getSearch_returnsSearchResult() throws Exception {
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of(
                new RideDto("r1", "d1", 59.4, 24.7, "Tallinn",
                        58.4, 26.7, "Tartu", "2026-05-01T10:00", BigDecimal.TEN, 2)
        ));
        when(reviewClient.getDriverRating(anyString(), any())).thenReturn(4.0);

        mockMvc.perform(get("/search")
                        .param("originLat", "59.4")
                        .param("originLon", "24.7")
                        .param("destinationLat", "58.4")
                        .param("destinationLon", "26.7")
                        .param("seatsNeeded", "1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.recommendations").isArray());
    }

    @Test
    void search_withCoordinatePairOnly_returns200() throws Exception {
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .param("originLat", "59.4")
                        .param("originLon", "24.7")
                        .param("destinationLat", "58.4")
                        .param("destinationLon", "26.7")
                        .param("seatsNeeded", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void search_withAddressPairOnly_geocodesAndReturns200() throws Exception {
        when(geolocationClient.geocode("Tallinn", null))
                .thenReturn(new com.esi.ridediscovery.domain.Location(59.4, 24.7, "Tallinn"));
        when(geolocationClient.geocode("Tartu", null))
                .thenReturn(new com.esi.ridediscovery.domain.Location(58.4, 26.7, "Tartu"));
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .param("originAddress", "Tallinn")
                        .param("destinationAddress", "Tartu")
                        .param("seatsNeeded", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criteria.origin.latitude").value(59.4))
                .andExpect(jsonPath("$.criteria.destination.longitude").value(26.7));
    }

    @Test
        void search_withBothPairs_returns200() throws Exception {
                when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                                anyString(), anyInt(), any())).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .param("originLat", "59.4")
                        .param("originLon", "24.7")
                        .param("destinationLat", "58.4")
                        .param("destinationLon", "26.7")
                        .param("originAddress", "Tallinn")
                        .param("destinationAddress", "Tartu")
                        .param("seatsNeeded", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void search_withNoPairs_returns400() throws Exception {
        mockMvc.perform(get("/search").param("seatsNeeded", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_zeroSeatsUsesDefaultSeatCount() throws Exception {
        when(rideBookingClient.searchRides(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), any())).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .param("originLat", "59.4")
                        .param("originLon", "24.7")
                        .param("destinationLat", "58.4")
                        .param("destinationLon", "26.7")
                        .param("seatsNeeded", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criteria.seatsNeeded").value(1));
    }
}
