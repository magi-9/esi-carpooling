package com.esi.ridebooking.bookings;

import java.util.UUID;

public class CreateBookingRequest {
    private UUID passengerId;

    public UUID getPassengerId() { return passengerId; }
    public void setPassengerId(UUID passengerId) { this.passengerId = passengerId; }
}
