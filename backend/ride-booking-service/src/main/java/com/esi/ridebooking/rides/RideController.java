package com.esi.ridebooking.rides;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esi.ridebooking.bookings.BookingDto;

@RestController
@RequestMapping("/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping
    public RideDto createRide(@RequestBody RideDto dto) {
        return rideService.createRide(dto);
    }

    @GetMapping
    public List<RideDto> getAllRides() {
        return rideService.getAllRides();
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
    public BookingDto createBooking(@PathVariable UUID rideId, @RequestBody BookingDto dto) {
        return rideService.createBooking(rideId, dto);
    }
}
