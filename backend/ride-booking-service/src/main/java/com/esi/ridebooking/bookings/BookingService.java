package com.esi.ridebooking.bookings;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return toDto(booking);
    }

    public void deleteBooking(UUID bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new EntityNotFoundException("Booking not found");
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
