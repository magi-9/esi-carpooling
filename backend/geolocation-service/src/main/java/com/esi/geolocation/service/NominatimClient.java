package com.esi.geolocation.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.esi.geolocation.dto.GeocodeResponse;
import com.esi.geolocation.dto.NominatimReverseResponse;
import com.esi.geolocation.dto.NominatimSearchResult;
import com.esi.geolocation.dto.ReverseGeocodeResponse;

@Service
public class NominatimClient {
    private final RestTemplate restTemplate;
    // Simple rate limiting: ensure at most 1 request per second
    private volatile long lastRequestMillis = 0L;

    // Minimum interval between requests in milliseconds (1000 ms = 1 request/sec)
    private final long minIntervalMillis;

    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    @Autowired
    public NominatimClient(RestTemplate restTemplate) {
        this(restTemplate, 1000L);
    }

    // package-visible for tests
    NominatimClient(RestTemplate restTemplate, long minIntervalMillis) {
        this.restTemplate = restTemplate;
        this.minIntervalMillis = minIntervalMillis;
    }

    public GeocodeResponse geocode(String address) {
        ensureRateLimit();
        URI uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .queryParam("limit", 1)
                .build()
                .toUri();

        ResponseEntity<NominatimSearchResult[]> resp = restTemplate.getForEntity(uri, NominatimSearchResult[].class);
        NominatimSearchResult[] results = resp.getBody();
        if (results == null || results.length == 0) {
            throw new IllegalArgumentException("Address not found");
        }
        NominatimSearchResult r = results[0];
        return new GeocodeResponse(Double.parseDouble(r.lat()), Double.parseDouble(r.lon()), r.display_name(), r.address());
    }

    public ReverseGeocodeResponse reverseGeocode(Double lat, Double lon) {
        ensureRateLimit();
        URI uri = UriComponentsBuilder.fromHttpUrl(REVERSE_URL)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("format", "json")
                .build()
                .toUri();

        ResponseEntity<NominatimReverseResponse> resp = restTemplate.getForEntity(uri, NominatimReverseResponse.class);
        NominatimReverseResponse body = resp.getBody();
        if (body == null) {
            throw new IllegalArgumentException("Reverse geocoding failed");
        }
        return new ReverseGeocodeResponse(Double.parseDouble(body.lat()), Double.parseDouble(body.lon()), body.display_name(), body.address());
    }

    private synchronized void ensureRateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestMillis;
        if (elapsed < minIntervalMillis) {
            try {
                Thread.sleep(minIntervalMillis - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestMillis = System.currentTimeMillis();
    }
}
