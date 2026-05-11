package com.esi.geolocation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esi.geolocation.dto.GeocodeResponse;
import com.esi.geolocation.dto.ReverseGeocodeResponse;
import com.esi.geolocation.service.NominatimClient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping
@Validated
public class GeolocationController {

    private final NominatimClient nominatimClient;

    public GeolocationController(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    @GetMapping("/geocode")
    public ResponseEntity<GeocodeResponse> geocode(@RequestParam @NotBlank String address) {
        GeocodeResponse r = nominatimClient.geocode(address);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<ReverseGeocodeResponse> reverseGeocode(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lon) {
        ReverseGeocodeResponse r = nominatimClient.reverseGeocode(lat, lon);
        return ResponseEntity.ok(r);
    }
}
