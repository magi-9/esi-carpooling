package com.esi.ridediscovery.controller;

import com.esi.ridediscovery.domain.Location;
import com.esi.ridediscovery.domain.RideRecommendation;
import com.esi.ridediscovery.domain.RideSearch;
import com.esi.ridediscovery.domain.SearchCriteria;
import com.esi.ridediscovery.dto.LocationResponse;
import com.esi.ridediscovery.dto.RideRecommendationResponse;
import com.esi.ridediscovery.dto.SearchCriteriaResponse;
import com.esi.ridediscovery.dto.SearchRequest;
import com.esi.ridediscovery.dto.SearchResponse;
import com.esi.ridediscovery.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/search")
@Tag(name = "Ride Discovery", description = "Search and ranking of available rides")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
        @Operation(summary = "Search rides", description = "Finds and ranks available rides using address or coordinate search criteria.")
    public ResponseEntity<SearchResponse> search(
            @ModelAttribute SearchRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {

        validateSearchRequest(request);

        SearchCriteria criteria = new SearchCriteria(
                new Location(toCoordinateOrNaN(request.originLat()), toCoordinateOrNaN(request.originLon()),
                        request.originAddress()),
                new Location(toCoordinateOrNaN(request.destinationLat()), toCoordinateOrNaN(request.destinationLon()),
                        request.destinationAddress()),
                request.departureDate() != null && !request.departureDate().isBlank()
                        ? LocalDate.parse(request.departureDate()) : null,
                request.seatsNeeded(),
                request.maxPricePerSeat()
        );

        RideSearch rideSearch = searchService.search(criteria, userId, authHeader);
        return ResponseEntity.ok(toSearchResponse(rideSearch));
    }

        private void validateSearchRequest(SearchRequest request) {
                boolean hasOriginCoordinates = request.originLat() != null && request.originLon() != null;
                boolean hasDestinationCoordinates = request.destinationLat() != null && request.destinationLon() != null;
                boolean hasCoordinatePair = hasOriginCoordinates && hasDestinationCoordinates;

                boolean hasAddressPair = !request.originAddress().isBlank() && !request.destinationAddress().isBlank();

                if (!hasCoordinatePair && !hasAddressPair) {
                        throw new IllegalArgumentException(
                                        "Either address pair or coordinate pair must be provided");
                }
        }

        private double toCoordinateOrNaN(Double coordinate) {
                return coordinate != null ? coordinate : Double.NaN;
        }

    private SearchResponse toSearchResponse(RideSearch s) {
        SearchCriteriaResponse criteriaResponse = new SearchCriteriaResponse(
                new LocationResponse(s.getCriteria().origin().latitude(),
                        s.getCriteria().origin().longitude(),
                        s.getCriteria().origin().displayAddress()),
                new LocationResponse(s.getCriteria().destination().latitude(),
                        s.getCriteria().destination().longitude(),
                        s.getCriteria().destination().displayAddress()),
                s.getCriteria().departureDate() != null ? s.getCriteria().departureDate().toString() : null,
                s.getCriteria().seatsNeeded(),
                s.getCriteria().maxPricePerSeat()
        );
        List<RideRecommendationResponse> recommendations = s.getRecommendations().stream()
                .map(this::toRecommendationResponse)
                .toList();
        return new SearchResponse(s.getSearchId(), s.getPassengerId(), s.getStatus().name(),
                s.getCreatedAt(), criteriaResponse, recommendations);
    }

        private RideRecommendationResponse toRecommendationResponse(RideRecommendation r) {
                return new RideRecommendationResponse(r.getRecommendationId(), r.getRideId(),
                                r.getRelevanceScore(), r.getDistanceToOriginKm(),
                                r.getDistanceToDestinationKm(), r.getDriverRating());
        }

}
