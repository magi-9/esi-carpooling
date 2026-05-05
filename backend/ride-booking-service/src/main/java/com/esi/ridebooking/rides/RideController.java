package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esi.ridebooking.bookings.BookingDto;

@RestController
@RequestMapping("/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createRide(@RequestHeader(value = "Authorization", required = false) String authHeader, @RequestBody CreateRideRequest request) {
        return rideService.createRide(request, authHeader).getRideId();
    }

    @GetMapping
    public List<RideDto> getAllRides(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDate,
            @RequestParam(required = false) Integer seatsNeeded,
            @RequestParam(required = false) BigDecimal maxPricePerSeat,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double originLat,
            @RequestParam(required = false) Double originLon,
            @RequestParam(required = false) Double destinationLat,
            @RequestParam(required = false) Double destinationLon,
            @RequestParam(required = false, defaultValue = "10.0") Double radiusKm) {
        return rideService.searchRides(departureDate, seatsNeeded, maxPricePerSeat, status,
                originLat, originLon, destinationLat, destinationLon, radiusKm);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideDto> getRide(@PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.getRideById(rideId));
    }

    @PutMapping("/{rideId}")
    public ResponseEntity<RideDto> updateRide(@PathVariable UUID rideId, @RequestBody RideDto dto) {
        return ResponseEntity.ok(rideService.updateRide(rideId, dto));
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<Void> deleteRide(@PathVariable UUID rideId) {
        rideService.deleteRide(rideId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createBooking(@PathVariable UUID rideId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return rideService.createBooking(rideId, authHeader).getBookingId();
    }
}
