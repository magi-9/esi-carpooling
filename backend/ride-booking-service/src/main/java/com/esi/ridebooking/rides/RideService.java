package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.esi.ridebooking.exception.ServiceUnavailableException;

import com.esi.ridebooking.bookings.Booking;
import com.esi.ridebooking.bookings.BookingDto;
import com.esi.ridebooking.bookings.BookingRepository;
import com.esi.ridebooking.util.JwtService;

@Service
public class RideService {

	@Autowired
	private RideRepository rideRepository;

	@Autowired
	private RideLocationRepository rideLocationRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private RestClient.Builder restClientBuilder;

	@Autowired
	private JwtService jwtService;

	@Value("${auth.service.url:http://localhost:8086}")
	private String authServiceUrl;

	@Value("${profile.service.url:http://localhost:8085}")
	private String profileServiceUrl;

	@Value("${geolocation.service.url:http://localhost:8088}")
	private String geolocationServiceUrl;

	@Value("${payment.service.url:http://localhost:8081}")
	private String paymentServiceUrl;

	public RideDto createRide(CreateRideRequest request, String authHeader) {
		// 1. Validate user has DRIVER role using local JWT parsing
		// Token is already validated by Spring Security OAuth2 Resource Server
		java.util.Set<String> roles = jwtService.extractRoles(authHeader);
		if (!roles.contains("DRIVER")) {
			throw new RuntimeException("User must have DRIVER role to create a ride");
		}

		// 2. Extract user ID from JWT token locally (instead of trusting request body)
		UUID currentUserId = jwtService.extractUserId(authHeader);

		// 3. Verify Vehicle with Profile Service
		// Check that the vehicle belongs to the driver AND is verified
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> vehicle = restClientBuilder.build().get()
					.uri(profileServiceUrl + "/profiles/" + currentUserId + "/vehicles/" + request.getVehicleId())
					.header("Authorization", authHeader)
					.retrieve()
					.body(Map.class);

			if (vehicle == null) {
				throw new IllegalArgumentException("Vehicle not found");
			}

			Boolean isVerified = (Boolean) vehicle.get("isVerified");
			if (isVerified == null || !isVerified) {
				throw new IllegalArgumentException("Vehicle must be verified before creating a ride");
			}
		} catch (ServiceUnavailableException e) {
			throw e;
		} catch (ResourceAccessException e) {
			throw new ServiceUnavailableException("Profile service unavailable", e);
		}

		// 4. Geocode addresses via Geolocation Service
		Map<String, Object> startCoords;
		Map<String, Object> endCoords;
		try {
			startCoords = restClientBuilder.build().get()
					.uri(geolocationServiceUrl + "/geocode?address=" + request.getStartAddress())
					.retrieve()
					.body(Map.class);

			endCoords = restClientBuilder.build().get()
					.uri(geolocationServiceUrl + "/geocode?address=" + request.getEndAddress())
					.retrieve()
					.body(Map.class);
		} catch (ResourceAccessException e) {
			throw new ServiceUnavailableException("Geolocation service unavailable", e);
		}

		// 5. Persist locations and ride
		RideLocation startLoc = new RideLocation();
		startLoc.setLatitude((Double) startCoords.get("latitude"));
		startLoc.setLongitude((Double) startCoords.get("longitude"));
		startLoc.setDisplayAddress(request.getStartAddress());
		rideLocationRepository.save(startLoc);

		RideLocation endLoc = new RideLocation();
		endLoc.setLatitude((Double) endCoords.get("latitude"));
		endLoc.setLongitude((Double) endCoords.get("longitude"));
		endLoc.setDisplayAddress(request.getEndAddress());
		rideLocationRepository.save(endLoc);

		Ride ride = new Ride();
		// Use authenticated user ID instead of request body
		ride.setDriverId(currentUserId);
		ride.setVehicleId(request.getVehicleId());
		ride.setAvailableSeats(request.getAvailableSeats());
		ride.setRideStartDate(request.getRideStartDate());
		ride.setSeatPriceAmount(request.getSeatPriceAmount());
		ride.setSeatPriceCurrency(request.getSeatPriceCurrency());
		ride.setStatus("PENDING");
		ride.setStartLocation(startLoc);
		ride.setEndLocation(endLoc);

		Ride savedRide = rideRepository.save(ride);
		return mapToDto(savedRide);
	}

	public List<RideDto> getAllRides() {
		return rideRepository.findAll().stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public List<RideDto> searchRides(LocalDateTime departureDate, Integer seatsNeeded, BigDecimal maxPricePerSeat,
			String status,
			Double originLat, Double originLon, Double destinationLat, Double destinationLon, Double radiusKm) {
		return rideRepository.findAll().stream()
				.filter(ride -> status == null || ride.getStatus().equals(status))
				.filter(ride -> departureDate == null ||
						(ride.getRideStartDate() != null &&
								ride.getRideStartDate().isAfter(departureDate.minusHours(1)) &&
								ride.getRideStartDate().isBefore(departureDate.plusHours(1))))
				.filter(ride -> seatsNeeded == null || ride.hasAvailableSeats())
				.filter(ride -> maxPricePerSeat == null ||
						(ride.getSeatPriceAmount() != null
								&& ride.getSeatPriceAmount().compareTo(maxPricePerSeat) <= 0))
				.filter(ride -> originLat == null || originLon == null ||
						isWithinRadius(ride.getStartLocation(), originLat, originLon, radiusKm))
				.filter(ride -> destinationLat == null || destinationLon == null ||
						isWithinRadius(ride.getEndLocation(), destinationLat, destinationLon, radiusKm))
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	private boolean isWithinRadius(RideLocation location, Double targetLat, Double targetLon, Double radiusKm) {
		if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
			return false;
		}
		double distance = calculateHaversineDistance(
				location.getLatitude(), location.getLongitude(),
				targetLat, targetLon);
		return distance <= radiusKm;
	}

	private double calculateHaversineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		final int R = 6371; // Earth's radius in kilometers
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
						* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	public RideDto getRideById(UUID rideId) {
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ride not found"));
		return mapToDto(ride);
	}

	public RideDto updateRide(UUID rideId, RideDto dto) {
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ride not found"));

		// Only allow updating mutable fields: date, price, currency, status
		// Do NOT allow updating: driverId, vehicleId, availableSeats (immutable)
		ride.setRideStartDate(dto.getRideStartDate());
		ride.setSeatPriceAmount(dto.getSeatPriceAmount());
		ride.setSeatPriceCurrency(dto.getSeatPriceCurrency());
		ride.setStatus(dto.getStatus());

		return mapToDto(rideRepository.save(ride));
	}

	public void deleteRide(UUID rideId) {
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ride not found"));

		// 1. Cancel all bookings for this ride
		List<Booking> bookings = bookingRepository.findByRideRideId(rideId);
		for (Booking booking : bookings) {
			if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
				booking.setStatus("CANCELLED");
				bookingRepository.save(booking);
			}
		}

		// 2. Mark ride as CANCELLED (soft delete) instead of hard deleting
		// This preserves the ride reference for historical bookings
		ride.setStatus("CANCELLED");
		rideRepository.save(ride);
	}

	public BookingDto createBooking(UUID rideId, String authHeader) {
		// 1. Validate passenger identity via Auth Service
		restClientBuilder.build().get()
				.uri(authServiceUrl + "/auth/validate")
				.header("Authorization", authHeader)
				.retrieve()
				.toBodilessEntity();

		// 2. Extract passenger ID from JWT token locally
		UUID passengerId = jwtService.extractUserId(authHeader);

		// 3. Check seat availability by counting confirmed bookings
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ride not found"));

		if (!ride.hasAvailableSeats()) {
			throw new IllegalArgumentException("No seats available for this ride");
		}

		// 4. Check for duplicate booking - prevent same passenger booking same ride
		// multiple times
		List<Booking> existingBookings = bookingRepository.findByRideRideId(rideId);
		boolean alreadyBooked = existingBookings.stream()
				.filter(b -> passengerId.equals(b.getPassengerId()))
				.filter(b -> !"CANCELLED".equals(b.getStatus()))
				.findAny()
				.isPresent();

		if (alreadyBooked) {
			throw new IllegalArgumentException("Passenger already has a booking for this ride");
		}

		// 5. Create pending booking
		Booking booking = new Booking();
		booking.setRide(ride);
		booking.setPassengerId(passengerId);
		booking.setStatus("PENDING");
		Booking savedBooking = bookingRepository.save(booking);

		// 6. Call Payment Service to authorize transaction
		try {
			Map<String, Object> paymentRequest = Map.of(
					"bookingId", savedBooking.getBookingId(),
					"amount", ride.getSeatPriceAmount(),
					"currency", ride.getSeatPriceCurrency(),
					"payerId", passengerId);

			Map<String, Object> paymentResponse = restClientBuilder.build().post()
					.uri(paymentServiceUrl + "/payments/authorize")
					.header("Authorization", authHeader)
					.body(paymentRequest)
					.retrieve()
					.body(Map.class);

			// 6. Payment succeeded - confirm booking
			savedBooking.setStatus("CONFIRMED");
			savedBooking.setPaymentId(UUID.fromString((String) paymentResponse.get("paymentId")));
			bookingRepository.save(savedBooking);

		} catch (ResourceAccessException e) {
			// Payment service unreachable - rollback and rethrow as 503
			bookingRepository.delete(savedBooking);
			throw new ServiceUnavailableException("Payment service unavailable", e);
		} catch (com.esi.ridebooking.exception.PaymentException e) {
			// Payment rejected/invalid - rollback and rethrow
			bookingRepository.delete(savedBooking);
			throw e;
		} catch (Exception e) {
			// Other errors - rollback and throw payment error
			bookingRepository.delete(savedBooking);
			throw new com.esi.ridebooking.exception.PaymentException("Payment authorization failed: " + e.getMessage());
		}

		return mapBookingToDto(savedBooking);
	}

	private RideDto mapToDto(Ride ride) {
		RideDto dto = new RideDto();
		dto.setRideId(ride.getRideId());
		dto.setDriverId(ride.getDriverId());
		dto.setVehicleId(ride.getVehicleId());
		dto.setAvailableSeats(ride.getAvailableSeats());
		dto.setRideStartDate(ride.getRideStartDate());
		dto.setSeatPriceAmount(ride.getSeatPriceAmount());
		dto.setSeatPriceCurrency(ride.getSeatPriceCurrency());
		dto.setStatus(ride.getStatus());

		if (ride.getStartLocation() != null) {
			dto.setStartAddress(ride.getStartLocation().getDisplayAddress());
			dto.setOriginLat(ride.getStartLocation().getLatitude());
			dto.setOriginLon(ride.getStartLocation().getLongitude());
		}
		if (ride.getEndLocation() != null) {
			dto.setEndAddress(ride.getEndLocation().getDisplayAddress());
			dto.setDestinationLat(ride.getEndLocation().getLatitude());
			dto.setDestinationLon(ride.getEndLocation().getLongitude());
		}

		return dto;
	}

	private BookingDto mapBookingToDto(Booking booking) {
		BookingDto dto = new BookingDto();
		dto.setBookingId(booking.getBookingId());
		dto.setRideId(booking.getRide() != null ? booking.getRide().getRideId() : null);
		dto.setPassengerId(booking.getPassengerId());
		dto.setPaymentId(booking.getPaymentId());
		dto.setStatus(booking.getStatus());
		return dto;
	}
}
