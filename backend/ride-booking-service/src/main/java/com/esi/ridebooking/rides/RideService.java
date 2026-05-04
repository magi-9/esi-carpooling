package com.esi.ridebooking.rides;

import java.util.Arrays;
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

    public RideDto createRide(RideDto dto, String authHeader) {
        // 1. Validate Auth Service (verify session)
        restClientBuilder.build().get()
                .uri(authServiceUrl + "/auth/validate")
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();

        // 2. Verify Driver Role
        String[] roles = restClientBuilder.build().get()
                .uri(authServiceUrl + "/auth/roles/" + dto.getDriverId())
                .header("Authorization", authHeader)
                .retrieve()
                .body(String[].class);
        
        if (roles == null || !Arrays.asList(roles).contains("Driver")) {
            throw new RuntimeException("User is not authorized as a Driver");
        }

        // 3. Verify Vehicle with Profile Service
        restClientBuilder.build().get()
                .uri(profileServiceUrl + "/profiles/" + dto.getDriverId() + "/vehicles")
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();

        // 4. Geocode addresses via Geolocation Service
        Map<String, Object> startCoords = restClientBuilder.build().get()
                .uri(geolocationServiceUrl + "/geocode?address=" + dto.getStartAddress())
                .retrieve()
                .body(Map.class);

        Map<String, Object> endCoords = restClientBuilder.build().get()
                .uri(geolocationServiceUrl + "/geocode?address=" + dto.getEndAddress())
                .retrieve()
                .body(Map.class);

        // 5. Persist locations and ride
        RideLocation startLoc = new RideLocation();
        startLoc.setLatitude((Double) startCoords.get("latitude"));
        startLoc.setLongitude((Double) startCoords.get("longitude"));
        startLoc.setDisplayAddress(dto.getStartAddress());
        rideLocationRepository.save(startLoc);

        RideLocation endLoc = new RideLocation();
        endLoc.setLatitude((Double) endCoords.get("latitude"));
        endLoc.setLongitude((Double) endCoords.get("longitude"));
        endLoc.setDisplayAddress(dto.getEndAddress());
        rideLocationRepository.save(endLoc);

        Ride ride = new Ride();
        ride.setDriverId(dto.getDriverId());
        ride.setVehicleId(dto.getVehicleId());
        ride.setAvailableSeats(dto.getAvailableSeats());
        ride.setRideStartDate(dto.getRideStartDate());
        ride.setSeatPriceAmount(dto.getSeatPriceAmount());
        ride.setSeatPriceCurrency(dto.getSeatPriceCurrency());
        ride.setStatus(dto.getStatus());
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

    public RideDto getRideById(UUID rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return mapToDto(ride);
    }

    public RideDto updateRide(UUID rideId, RideDto dto) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setDriverId(dto.getDriverId());
        ride.setVehicleId(dto.getVehicleId());
        ride.setAvailableSeats(dto.getAvailableSeats());
        ride.setRideStartDate(dto.getRideStartDate());
        ride.setSeatPriceAmount(dto.getSeatPriceAmount());
        ride.setSeatPriceCurrency(dto.getSeatPriceCurrency());
        ride.setStatus(dto.getStatus());

        return mapToDto(rideRepository.save(ride));
    }

    public void deleteRide(UUID rideId) {
        if (!rideRepository.existsById(rideId)) {
            throw new RuntimeException("Ride not found");
        }
        rideRepository.deleteById(rideId);
    }

    public BookingDto createBooking(UUID rideId, BookingDto dto, String authHeader) {
        // 1. Validate passenger identity via Auth Service
        restClientBuilder.build().get()
                .uri(authServiceUrl + "/auth/validate")
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();

        // 2. Check seat availability
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        
        if (ride.getAvailableSeats() <= 0) {
            throw new RuntimeException("No seats available for this ride");
        }

        // 3. Create pending booking
        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassengerId(dto.getPassengerId());
        booking.setStatus("PENDING");
        Booking savedBooking = bookingRepository.save(booking);

        // 4. Call Payment Service to authorize transaction
        try {
            Map<String, Object> paymentRequest = Map.of(
                    "bookingId", savedBooking.getBookingId(),
                    "amount", ride.getSeatPriceAmount(),
                    "currency", ride.getSeatPriceCurrency(),
                    "payerId", dto.getPassengerId()
            );

            Map<String, Object> paymentResponse = restClientBuilder.build().post()
                    .uri(paymentServiceUrl + "/payments/authorize")
                    .header("Authorization", authHeader)
                    .body(paymentRequest)
                    .retrieve()
                    .body(Map.class);

            // 5. Payment succeeded - confirm booking and reduce available seats
            savedBooking.setStatus("CONFIRMED");
            savedBooking.setPaymentId(UUID.fromString((String) paymentResponse.get("paymentId")));
            bookingRepository.save(savedBooking);

            ride.setAvailableSeats(ride.getAvailableSeats() - 1);
            rideRepository.save(ride);

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
