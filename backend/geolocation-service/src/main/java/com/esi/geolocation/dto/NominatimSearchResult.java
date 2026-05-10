package com.esi.geolocation.dto;

import java.util.Map;

public class NominatimSearchResult {
    private String lat;
    private String lon;
    private String display_name;
    private Map<String, Object> address;

    public NominatimSearchResult() {
    }

    public String lat() {
        return lat;
    }

    public String lon() {
        return lon;
    }

    public String display_name() {
        return display_name;
    }

    public Map<String, Object> address() {
        return address;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }
}
