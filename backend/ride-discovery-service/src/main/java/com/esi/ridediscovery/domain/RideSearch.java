package com.esi.ridediscovery.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root representing a ride search session.
 */
public class RideSearch {

    private final UUID searchId;
    private final String passengerId;
    private SearchStatus status;
    private final Instant createdAt;
    private final SearchCriteria criteria;
    private final List<RideRecommendation> recommendations;

    private RideSearch(UUID searchId, String passengerId, SearchCriteria criteria) {
        this.searchId = searchId;
        this.passengerId = passengerId;
        this.status = SearchStatus.PENDING;
        this.createdAt = Instant.now();
        this.criteria = criteria;
        this.recommendations = new ArrayList<>();
    }

    public static RideSearch create(String passengerId, SearchCriteria criteria) {
        return new RideSearch(UUID.randomUUID(), passengerId, criteria);
    }

    public void addRecommendations(List<RideRecommendation> newRecommendations) {
        this.recommendations.addAll(newRecommendations);
        this.status = SearchStatus.COMPLETED;
    }

    public void fail() {
        this.status = SearchStatus.FAILED;
    }

    public UUID getSearchId() {
        return searchId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public SearchStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }

    public List<RideRecommendation> getRecommendations() {
        return Collections.unmodifiableList(recommendations);
    }
}
