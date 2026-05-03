package com.esi.ridebooking.rides;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public RideDto createRide(RideDto dto) {
        RideLocation startLoc = new RideLocation();
        startLoc.setLatitude(dto.getStartLocation().getLatitude());
        startLoc.setLongitude(dto.getStartLocation().getLongitude());
        startLoc.setDisplayAddress(dto.getStartLocation().getDisplayAddress());
        rideLocationRepository.save(startLoc);

        RideLocation endLoc = new RideLocation();
        endLoc.setLatitude(dto.getEndLocation().getLatitude());
        endLoc.setLongitude(dto.getEndLocation().getLongitude());
        endLoc.setDisplayAddress(dto.getEndLocation().getDisplayAddress());
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

        if (dto.getStartLocation() != null) {
            RideLocation startLoc = ride.getStartLocation();
            if (startLoc == null) {
                startLoc = new RideLocation();
            }
            startLoc.setLatitude(dto.getStartLocation().getLatitude());
            startLoc.setLongitude(dto.getStartLocation().getLongitude());
            startLoc.setDisplayAddress(dto.getStartLocation().getDisplayAddress());
            rideLocationRepository.save(startLoc);
            ride.setStartLocation(startLoc);
        }

        if (dto.getEndLocation() != null) {
            RideLocation endLoc = ride.getEndLocation();
            if (endLoc == null) {
                endLoc = new RideLocation();
            }
            endLoc.setLatitude(dto.getEndLocation().getLatitude());
            endLoc.setLongitude(dto.getEndLocation().getLongitude());
            endLoc.setDisplayAddress(dto.getEndLocation().getDisplayAddress());
            rideLocationRepository.save(endLoc);
            ride.setEndLocation(endLoc);
        }

        return mapToDto(rideRepository.save(ride));
    }

    public void deleteRide(UUID rideId) {
        if (!rideRepository.existsById(rideId)) {
            throw new RuntimeException("Ride not found");
        }
        rideRepository.deleteById(rideId);
    }

    public BookingDto createBooking(UUID rideId, BookingDto dto) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassengerId(dto.getPassengerId());
        booking.setPaymentId(dto.getPaymentId());
        booking.setStatus(dto.getStatus());

        Booking savedBooking = bookingRepository.save(booking);
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
            RideLocationDto startDto = new RideLocationDto();
            startDto.setRideLocationId(ride.getStartLocation().getRideLocationId());
            startDto.setLatitude(ride.getStartLocation().getLatitude());
            startDto.setLongitude(ride.getStartLocation().getLongitude());
            startDto.setDisplayAddress(ride.getStartLocation().getDisplayAddress());
            dto.setStartLocation(startDto);
        }

        if (ride.getEndLocation() != null) {
            RideLocationDto endDto = new RideLocationDto();
            endDto.setRideLocationId(ride.getEndLocation().getRideLocationId());
            endDto.setLatitude(ride.getEndLocation().getLatitude());
            endDto.setLongitude(ride.getEndLocation().getLongitude());
            endDto.setDisplayAddress(ride.getEndLocation().getDisplayAddress());
            dto.setEndLocation(endDto);
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
