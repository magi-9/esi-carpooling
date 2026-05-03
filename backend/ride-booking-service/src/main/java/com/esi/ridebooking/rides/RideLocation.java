package com.esi.ridebooking.rides;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ride_location")
public class RideLocation {

	@Id
	private UUID rideLocationId;

	private Double latitude;

	private Double longitude;

	private String displayAddress;

	public RideLocation() {
	}

	public UUID getRideLocationId() {
		return rideLocationId;
	}

	public void setRideLocationId(UUID rideLocationId) {
		this.rideLocationId = rideLocationId;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getDisplayAddress() {
		return displayAddress;
	}

	public void setDisplayAddress(String displayAddress) {
		this.displayAddress = displayAddress;
	}
}
