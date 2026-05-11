package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.esi.ridebooking.bookings.BookingDto;
import com.esi.ridebooking.exception.PaymentException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(RideController.class)
@AutoConfigureMockMvc(addFilters = false)
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

		String rideJson = """
				{
				    "vehicleId": "550e8400-e29b-41d4-a716-446655440000",
				    "availableSeats": 3,
				    "rideStartDate": "2026-05-05T10:00:00",
				    "seatPriceAmount": 25.50,
				    "seatPriceCurrency": "EUR",
				    "startAddress": "Vilnius",
				    "endAddress": "Kaunas"
				}
				""";

		mockMvc.perform(post("/rides")
				.header("Authorization", "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(rideJson))
				.andExpect(status().isCreated())
				.andExpect(content().string("\"" + responseDto.getRideId().toString() + "\""));
	}

	@Test
	void createRide_ErrorCase() throws Exception {
		when(rideService.createRide(any(CreateRideRequest.class), anyString()))
				.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "User is not authorized as a Driver"));

		mockMvc.perform(post("/rides")
				.header("Authorization", "Bearer invalid-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						    "vehicleId": "00000000-0000-0000-0000-000000000000",
						    "availableSeats": 1,
						    "rideStartDate": "2026-05-05T10:00:00",
						    "seatPriceAmount": 10.00,
						    "seatPriceCurrency": "EUR",
						    "startAddress": "A",
						    "endAddress": "B"
						}
						"""))
				.andExpect(status().isUnauthorized());
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

		when(rideService.createBooking(eq(rideId), anyString()))
				.thenReturn(responseDto);

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token"))
				.andExpect(status().isCreated());
	}

	@Test
	void createBooking_NoSeatsAvailable() throws Exception {
		UUID rideId = UUID.randomUUID();

		when(rideService.createBooking(eq(rideId), anyString()))
				.thenThrow(new IllegalArgumentException("No seats available for this ride"));

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createBooking_PaymentFailure() throws Exception {
		UUID rideId = UUID.randomUUID();

		when(rideService.createBooking(eq(rideId), anyString()))
				.thenThrow(new PaymentException("Payment authorization failed"));

		mockMvc.perform(post("/rides/{rideId}/bookings", rideId)
				.header("Authorization", "Bearer valid-token"))
				.andExpect(status().isPaymentRequired());
	}
}
