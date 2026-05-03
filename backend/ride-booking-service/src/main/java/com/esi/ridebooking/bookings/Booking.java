package com.esi.ridebooking.bookings;

import java.util.UUID;

import com.esi.ridebooking.rides.Ride;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    private UUID bookingId;

    @ManyToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;

    private UUID passengerId;

    private UUID paymentId;

    private String status;

    public Booking() {
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public UUID getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(UUID passengerId) {
        this.passengerId = passengerId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
