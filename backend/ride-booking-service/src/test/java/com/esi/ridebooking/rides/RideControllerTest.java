package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.esi.ridebooking.bookings.BookingDto;
import com.esi.ridebooking.bookings.CreateBookingRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideController.class)
public class RideControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RideService rideService;

	@Test
	void createRide_HappyPath() throws Exception {
		UUID driverId = UUID.randomUUID();
		UUID vehicleId = UUID.randomUUID();

		RideDto responseDto = new RideDto();
		responseDto.setRideId(UUID.randomUUID());
		responseDto.setDriverId(driverId);
		responseDto.setStartAddress("Vilnius");
		responseDto.setEndAddress("Kaunas");

		when(rideService.createRide(any(CreateRideRequest.class), anyString())).thenReturn(responseDto);

		String rideJson = String.format("""
				{
				    "driverId": "%s",
				    "vehicleId": "%s",
				    "availableSeats": 3,
				    "rideStartDate": "2026-05-05T10:00:00",
				    "seatPriceAmount": 25.50,
				    "seatPriceCurrency": "EUR",
				    "startAddress": "Vilnius",
				    "endAddress": "Kaunas"
				}
				""", driverId, vehicleId);

		mockMvc.perform(post("/rides")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(rideJson))
				.andExpect(status().isCreated());
	}

	@Test
	void createRide_ErrorCase() throws Exception {
		when(rideService.createRide(any(CreateRideRequest.class), anyString()))
				.thenThrow(new RuntimeException("User is not authorized as a Driver"));

		mockMvc.perform(post("/rides")
				.header("Authorization", "Bearer invalid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						    "driverId": "00000000-0000-0000-0000-000000000000",
						    "vehicleId": "00000000-0000-0000-0000-000000000000",
						    "availableSeats": 1,
						    "rideStartDate": "2026-05-05T10:00:00",
						    "seatPriceAmount": 10.00,
						    "seatPriceCurrency": "EUR",
						    "startAddress": "A",
						    "endAddress": "B"
						}
						"""))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void createBooking_HappyPath() throws Exception {
		UUID rideId = UUID.randomUUID();
		UUID bookingId = UUID.randomUUID();
		UUID passengerId = UUID.randomUUID();

		BookingDto responseDto = new BookingDto();
		responseDto.setBookingId(bookingId);
		responseDto.setRideId(rideId);
		responseDto.setPassengerId(passengerId);
		responseDto.setStatus("CONFIRMED");

		when(rideService.createBooking(eq(rideId), any(CreateBookingRequest.class), anyString())).thenReturn(responseDto);

		String bookingJson = String.format("""
				{
				    "passengerId": "%s"
				}
				""", passengerId);

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookingJson))
				.andExpect(status().isCreated());
	}

	@Test
	void createBooking_NoSeatsAvailable() throws Exception {
		UUID rideId = UUID.randomUUID();
		UUID passengerId = UUID.randomUUID();

		when(rideService.createBooking(eq(rideId), any(CreateBookingRequest.class), anyString()))
				.thenThrow(new RuntimeException("No seats available for this ride"));

		String bookingJson = String.format("""
				{
				    "passengerId": "%s"
				}
				""", passengerId);

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookingJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void createBooking_PaymentFailure() throws Exception {
		UUID rideId = UUID.randomUUID();
		UUID passengerId = UUID.randomUUID();

		when(rideService.createBooking(eq(rideId), any(CreateBookingRequest.class), anyString()))
				.thenThrow(new RuntimeException("Payment authorization failed"));

		String bookingJson = String.format("""
				{
				    "passengerId": "%s"
				}
				""", passengerId);

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookingJson))
				.andExpect(status().isInternalServerError());
	}
}
