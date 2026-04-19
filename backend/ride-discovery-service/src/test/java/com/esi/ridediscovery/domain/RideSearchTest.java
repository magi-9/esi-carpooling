package com.esi.ridediscovery.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideSearchTest {

    private SearchCriteria buildCriteria() {
        return new SearchCriteria(
                new Location(59.4, 24.7, "Tallinn"),
                new Location(58.4, 26.7, "Tartu"),
                LocalDate.now().plusDays(1),
                1,
                BigDecimal.valueOf(20)
        );
    }

    @Test
    void create_setsInitialState() {
        RideSearch search = RideSearch.create("user-1", buildCriteria());
        assertThat(search.getSearchId()).isNotNull();
        assertThat(search.getPassengerId()).isEqualTo("user-1");
        assertThat(search.getStatus()).isEqualTo(SearchStatus.PENDING);
        assertThat(search.getRecommendations()).isEmpty();
        assertThat(search.getCreatedAt()).isNotNull();
    }

    @Test
    void addRecommendations_transitionsToCompleted() {
        RideSearch search = RideSearch.create("user-1", buildCriteria());
        List<RideRecommendation> recs = List.of(
                RideRecommendation.of("ride-1", 0.8, 0.5, 1.2, 4.5)
        );
        search.addRecommendations(recs);
        assertThat(search.getStatus()).isEqualTo(SearchStatus.COMPLETED);
        assertThat(search.getRecommendations()).hasSize(1);
    }

    @Test
    void addRecommendations_empty_stillTransitionsToCompleted() {
        RideSearch search = RideSearch.create("user-1", buildCriteria());
        search.addRecommendations(List.of());
        assertThat(search.getStatus()).isEqualTo(SearchStatus.COMPLETED);
        assertThat(search.getRecommendations()).isEmpty();
    }

    @Test
    void fail_transitionsToFailed() {
        RideSearch search = RideSearch.create("user-1", buildCriteria());
        search.fail();
        assertThat(search.getStatus()).isEqualTo(SearchStatus.FAILED);
    }

    @Test
    void recommendations_areImmutable() {
        RideSearch search = RideSearch.create("user-1", buildCriteria());
        search.addRecommendations(List.of(RideRecommendation.of("ride-1", 0.9, 0.1, 0.2, 5.0)));
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> search.getRecommendations().add(RideRecommendation.of("ride-2", 0.5, 1.0, 1.0, 3.0)));
    }
}
