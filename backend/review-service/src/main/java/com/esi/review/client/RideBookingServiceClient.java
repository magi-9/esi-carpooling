package com.esi.review.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.esi.review.exception.ServiceUnavailableException;

@Component
public class RideBookingServiceClient {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Value("${clients.ride-booking-service-url}")
    private String rideBookingServiceUrl;

    public BookingDto getBooking(UUID bookingId) {
        try {
            return restClientBuilder
                    .baseUrl(rideBookingServiceUrl)
                    .build()
                    .get()
                    .uri("/bookings/{bookingId}", bookingId)
                    .retrieve()
                    .body(BookingDto.class);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("Ride booking service unavailable", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch booking: " + e.getMessage());
        }
    }

    public static class BookingDto {
        private UUID bookingId;
        private UUID rideId;
        private UUID passengerId;
        private UUID paymentId;
        private String status;

        public UUID getBookingId() {
            return bookingId;
        }

        public void setBookingId(UUID bookingId) {
            this.bookingId = bookingId;
        }

        public UUID getRideId() {
            return rideId;
        }

        public void setRideId(UUID rideId) {
            this.rideId = rideId;
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
}
