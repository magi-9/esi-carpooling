package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.esi.ridebooking.bookings.Booking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ride")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID rideId;

    private UUID driverId;

    private UUID vehicleId;

    private Integer availableSeats;

    private LocalDateTime rideStartDate;

    private BigDecimal seatPriceAmount;

    private String seatPriceCurrency;

    private String status;

    @ManyToOne
    @JoinColumn(name = "start_location_id")
    private RideLocation startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id")
    private RideLocation endLocation;

    @OneToMany(mappedBy = "ride")
    private List<Booking> bookings = new ArrayList<>();

    public Ride() {
    }

    public int getConfirmedBookingsCount() {
        return (int) bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .count();
    }

    public boolean hasAvailableSeats() {
        return getConfirmedBookingsCount() < availableSeats;
    }

    public UUID getRideId() {
        return rideId;
    }

    public void setRideId(UUID rideId) {
        this.rideId = rideId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public LocalDateTime getRideStartDate() {
        return rideStartDate;
    }

    public void setRideStartDate(LocalDateTime rideStartDate) {
        this.rideStartDate = rideStartDate;
    }

    public BigDecimal getSeatPriceAmount() {
        return seatPriceAmount;
    }

    public void setSeatPriceAmount(BigDecimal seatPriceAmount) {
        this.seatPriceAmount = seatPriceAmount;
    }

    public String getSeatPriceCurrency() {
        return seatPriceCurrency;
    }

    public void setSeatPriceCurrency(String seatPriceCurrency) {
        this.seatPriceCurrency = seatPriceCurrency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RideLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(RideLocation startLocation) {
        this.startLocation = startLocation;
    }

    public RideLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(RideLocation endLocation) {
        this.endLocation = endLocation;
    }
}
