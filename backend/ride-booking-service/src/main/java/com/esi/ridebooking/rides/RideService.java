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
import org.springframework.web.client.RestClient;

import com.esi.ridebooking.bookings.Booking;
import com.esi.ridebooking.bookings.BookingDto;
import com.esi.ridebooking.bookings.BookingRepository;
import com.esi.ridebooking.bookings.CreateBookingRequest;

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

	@Value("${auth.service.url:http://localhost:8086}")
	private String authServiceUrl;

	@Value("${profile.service.url:http://localhost:8085}")
	private String profileServiceUrl;

	@Value("${geolocation.service.url:http://localhost:8088}")
	private String geolocationServiceUrl;

	@Value("${payment.service.url:http://localhost:8081}")
	private String paymentServiceUrl;

	private UUID getCurrentUserId(String authHeader) {
		// Call auth service to get current user info from token
		// Returns userId extracted from the validated JWT token
		Map<String, Object> userInfo = restClientBuilder.build().get()
				.uri(authServiceUrl + "/auth/user")
				.header("Authorization", authHeader)
				.retrieve()
				.body(Map.class);
		
		if (userInfo == null || userInfo.get("userId") == null) {
			throw new RuntimeException("Unable to get current user from token");
		}
		return UUID.fromString((String) userInfo.get("userId"));
	}

	public RideDto createRide(CreateRideRequest request, String authHeader) {
		// 1. Validate Auth Service (verify session and Driver role)
		// First check token is valid
		restClientBuilder.build().get()
				.uri(authServiceUrl + "/auth/validate")
				.header("Authorization", authHeader)
				.retrieve()
				.toBodilessEntity();

		// Then check user has DRIVER role (returns 200 if has role, 403 if not)
		restClientBuilder.build().get()
				.uri(authServiceUrl + "/auth/validate/role/DRIVER")
				.header("Authorization", authHeader)
				.retrieve()
				.toBodilessEntity();

		// 2. Get current user ID from auth service (instead of trusting request)
		UUID currentUserId = getCurrentUserId(authHeader);

		// 3. Verify Vehicle with Profile Service (TODO: implement when profile service
		// is available)
		// Currently skipped as profile service is not yet implemented
		// restClientBuilder.build().get()
		// .uri(profileServiceUrl + "/profiles/" + currentUserId + "/vehicles")
		// .header("Authorization", authHeader)
		// .retrieve()
		// .toBodilessEntity();

		// 4. Geocode addresses via Geolocation Service
		Map<String, Object> startCoords = restClientBuilder.build().get()
				.uri(geolocationServiceUrl + "/geocode?address=" + request.getStartAddress())
				.retrieve()
				.body(Map.class);

		Map<String, Object> endCoords = restClientBuilder.build().get()
				.uri(geolocationServiceUrl + "/geocode?address=" + request.getEndAddress())
				.retrieve()
				.body(Map.class);

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
				.orElseThrow(() -> new RuntimeException("Ride not found"));
		return mapToDto(ride);
	}

	public RideDto updateRide(UUID rideId, RideDto dto) {
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new RuntimeException("Ride not found"));

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
				.orElseThrow(() -> new RuntimeException("Ride not found"));
		
		// 1. Cancel all bookings for this ride before deleting
		List<Booking> bookings = bookingRepository.findByRideRideId(rideId);
		for (Booking booking : bookings) {
			if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
				booking.setStatus("CANCELLED");
				bookingRepository.save(booking);
			}
		}
		
		// 2. Delete the ride
		rideRepository.deleteById(rideId);
	}

	public BookingDto createBooking(UUID rideId, CreateBookingRequest request, String authHeader) {
		// 1. Validate passenger identity via Auth Service
		restClientBuilder.build().get()
				.uri(authServiceUrl + "/auth/validate")
				.header("Authorization", authHeader)
				.retrieve()
				.toBodilessEntity();

		// 2. Check seat availability by counting confirmed bookings
		Ride ride = rideRepository.findById(rideId)
				.orElseThrow(() -> new RuntimeException("Ride not found"));

		if (!ride.hasAvailableSeats()) {
			throw new RuntimeException("No seats available for this ride");
		}

		// 3. Check for duplicate booking - prevent same passenger booking same ride multiple times
		List<Booking> existingBookings = bookingRepository.findByRideRideId(rideId);
		boolean alreadyBooked = existingBookings.stream()
				.filter(b -> request.getPassengerId().equals(b.getPassengerId()))
				.filter(b -> !"CANCELLED".equals(b.getStatus()))
				.findAny()
				.isPresent();
		
		if (alreadyBooked) {
			throw new RuntimeException("Passenger already has a booking for this ride");
		}

		// 4. Create pending booking
		Booking booking = new Booking();
		booking.setRide(ride);
		booking.setPassengerId(request.getPassengerId());
		booking.setStatus("PENDING");
		Booking savedBooking = bookingRepository.save(booking);

		// 5. Call Payment Service to authorize transaction
		try {
			Map<String, Object> paymentRequest = Map.of(
					"bookingId", savedBooking.getBookingId(),
					"amount", ride.getSeatPriceAmount(),
					"currency", ride.getSeatPriceCurrency(),
					"payerId", request.getPassengerId());

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

		} catch (Exception e) {
			// Payment failed - rollback by deleting pending booking
			bookingRepository.delete(savedBooking);
			throw new RuntimeException("Payment authorization failed: " + e.getMessage());
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
		}
		if (ride.getEndLocation() != null) {
			dto.setEndAddress(ride.getEndLocation().getDisplayAddress());
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
