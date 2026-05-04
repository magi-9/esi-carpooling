package com.esi.ridebooking.rides;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RideDto {
    private UUID rideId;
    private UUID driverId;
    private UUID vehicleId;
    private Integer availableSeats;
    private LocalDateTime rideStartDate;
    private BigDecimal seatPriceAmount;
    private String seatPriceCurrency;
    private String status;
    private String startAddress;
    private String endAddress;

    public UUID getRideId() { return rideId; }
    public void setRideId(UUID rideId) { this.rideId = rideId; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public LocalDateTime getRideStartDate() { return rideStartDate; }
    public void setRideStartDate(LocalDateTime rideStartDate) { this.rideStartDate = rideStartDate; }
    public BigDecimal getSeatPriceAmount() { return seatPriceAmount; }
    public void setSeatPriceAmount(BigDecimal seatPriceAmount) { this.seatPriceAmount = seatPriceAmount; }
    public String getSeatPriceCurrency() { return seatPriceCurrency; }
    public void setSeatPriceCurrency(String seatPriceCurrency) { this.seatPriceCurrency = seatPriceCurrency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }
    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }
}
