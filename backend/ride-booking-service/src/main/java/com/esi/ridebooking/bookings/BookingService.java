package com.esi.ridebooking.bookings;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esi.ridebooking.rides.Ride;
import com.esi.ridebooking.rides.RideRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RideRepository rideRepository;

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByPassengerId(UUID passengerId) {
        return bookingRepository.findByPassengerId(passengerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return toDto(booking);
    }

    public List<BookingDto> getBookingsByRideId(UUID rideId, UUID userId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));
        if (!ride.getDriverId().equals(userId)) {
            throw new IllegalArgumentException("You can only view bookings for your own rides");
        }
        return bookingRepository.findByRideRideId(rideId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void deleteBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        if (!booking.getPassengerId().equals(userId)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }
        bookingRepository.deleteById(bookingId);
    }

    private BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setBookingId(booking.getBookingId());
        dto.setRideId(booking.getRide() != null ? booking.getRide().getRideId() : null);
        dto.setPassengerId(booking.getPassengerId());
        dto.setPaymentId(booking.getPaymentId());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
