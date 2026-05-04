package com.esi.ridebooking.bookings;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void getBooking_HappyPath() throws Exception {
        UUID bookingId = UUID.randomUUID();

        BookingDto responseDto = new BookingDto();
        responseDto.setBookingId(bookingId);
        responseDto.setStatus("CONFIRMED");

        when(bookingService.getBookingById(bookingId)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBooking_NotFound() throws Exception {
        UUID nonExistentBookingId = UUID.randomUUID();

        when(bookingService.getBookingById(nonExistentBookingId))
                .thenThrow(new EntityNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", nonExistentBookingId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_HappyPath() throws Exception {
        UUID bookingId = UUID.randomUUID();

        mockMvc.perform(delete("/bookings/{bookingId}", bookingId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBooking_NotFound() throws Exception {
        UUID nonExistentBookingId = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Booking not found"))
                .when(bookingService).deleteBooking(nonExistentBookingId);

        mockMvc.perform(delete("/bookings/{bookingId}", nonExistentBookingId))
                .andExpect(status().isNotFound());
    }
}
