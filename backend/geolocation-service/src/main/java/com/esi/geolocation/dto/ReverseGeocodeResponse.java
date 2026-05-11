package com.esi.geolocation.dto;

import java.util.Map;

public class ReverseGeocodeResponse {
    private double latitude;
    private double longitude;
    private String displayName;
    private Map<String, Object> address;

    public ReverseGeocodeResponse() {
    }

    public ReverseGeocodeResponse(double latitude, double longitude, String displayName, Map<String, Object> address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Object> getAddress() {
        return address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }
}
