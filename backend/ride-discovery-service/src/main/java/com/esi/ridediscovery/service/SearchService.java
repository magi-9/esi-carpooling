package com.esi.ridediscovery.service;

import com.esi.ridediscovery.client.GeolocationClient;
import com.esi.ridediscovery.client.ReviewClient;
import com.esi.ridediscovery.client.RideBookingClient;
import com.esi.ridediscovery.client.dto.RideDto;
import com.esi.ridediscovery.domain.Location;
import com.esi.ridediscovery.domain.RideRecommendation;
import com.esi.ridediscovery.domain.RideSearch;
import com.esi.ridediscovery.domain.SearchCriteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchService {

    private final GeolocationClient geolocationClient;
    private final RideBookingClient rideBookingClient;
    private final ReviewClient reviewClient;

    public SearchService(
            GeolocationClient geolocationClient,
            RideBookingClient rideBookingClient,
            ReviewClient reviewClient) {
        this.geolocationClient = geolocationClient;
        this.rideBookingClient = rideBookingClient;
        this.reviewClient = reviewClient;
    }

    public RideSearch search(SearchCriteria criteria, String passengerId, String authHeader) {
        Location resolvedOrigin = resolveLocation(criteria.origin(), authHeader);
        Location resolvedDestination = resolveLocation(criteria.destination(), authHeader);
        SearchCriteria resolvedCriteria = new SearchCriteria(
                resolvedOrigin,
                resolvedDestination,
                criteria.departureDate(),
                criteria.seatsNeeded(),
                criteria.maxPricePerSeat()
        );

        RideSearch rideSearch = RideSearch.create(passengerId, resolvedCriteria);

        try {
            List<RideDto> rides = rideBookingClient.searchRides(
                    String.valueOf(resolvedOrigin.latitude()),
                    String.valueOf(resolvedOrigin.longitude()),
                    String.valueOf(resolvedDestination.latitude()),
                    String.valueOf(resolvedDestination.longitude()),
                    resolvedCriteria.departureDate() != null ? resolvedCriteria.departureDate().toString() : "",
                    resolvedCriteria.seatsNeeded(),
                    authHeader
            );

            // Find max price for normalization
            BigDecimal maxPrice = rides.stream()
                    .map(RideDto::pricePerSeat)
                    .filter(p -> p != null)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);

            if (maxPrice.compareTo(BigDecimal.ZERO) == 0) {
                maxPrice = BigDecimal.ONE;
            }

            List<RideRecommendation> recommendations = new ArrayList<>();

            for (RideDto ride : rides) {
                double driverRating = reviewClient.getDriverRating(ride.driverId(), authHeader);

                double normalizedRating = driverRating / 5.0;

                BigDecimal pricePerSeat = ride.pricePerSeat() != null ? ride.pricePerSeat() : BigDecimal.ZERO;
                double normalizedPrice = maxPrice.compareTo(BigDecimal.ZERO) != 0
                        ? pricePerSeat.doubleValue() / maxPrice.doubleValue()
                        : 0.0;

                double distanceToOriginKm = haversineDistance(
                    resolvedOrigin.latitude(), resolvedOrigin.longitude(),
                        ride.originLat(), ride.originLon()
                );

                double distanceToDestinationKm = haversineDistance(
                    resolvedDestination.latitude(), resolvedDestination.longitude(),
                        ride.destinationLat(), ride.destinationLon()
                );

                double normalizedDistance = Math.min(distanceToOriginKm / 10.0, 1.0);

                double relevanceScore = 0.4 * normalizedRating
                        + 0.3 * (1.0 - normalizedPrice)
                        + 0.3 * (1.0 - normalizedDistance);

                recommendations.add(RideRecommendation.of(
                        ride.rideId(),
                        relevanceScore,
                        distanceToOriginKm,
                        distanceToDestinationKm,
                        driverRating
                ));
            }

            recommendations.sort(Comparator.comparingDouble(RideRecommendation::getRelevanceScore).reversed());
            rideSearch.addRecommendations(recommendations);

        } catch (Exception e) {
            rideSearch.fail();
        }
        return rideSearch;
    }

    private Location resolveLocation(Location location, String authHeader) {
        boolean hasCoordinates = !Double.isNaN(location.latitude()) && !Double.isNaN(location.longitude());
        boolean hasAddress = location.displayAddress() != null && !location.displayAddress().isBlank();

        if (hasCoordinates) {
            return location;
        }
        if (hasAddress) {
            return geolocationClient.geocode(location.displayAddress(), authHeader);
        }
        throw new IllegalArgumentException("Either coordinates or address must be provided for each location");
    }

    /**
     * Haversine formula to compute distance in kilometers between two lat/lon points.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
