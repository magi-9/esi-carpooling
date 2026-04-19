# Ride Discovery Service

Stateless aggregation service that finds and ranks available rides for passengers.

## Overview

This service has **no database**. It calls three external services, aggregates the results, ranks them by relevance, and returns the ranked list. Search results are stored in-memory and are ephemeral (lost on restart).

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/search` | Search for rides matching criteria |
| GET | `/search/{searchId}` | Retrieve a past search by ID |
| GET | `/search/{searchId}/recommendations` | Get ranked recommendations |

### Search Query Parameters

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `originLat` | double | yes | Origin latitude |
| `originLon` | double | yes | Origin longitude |
| `originAddress` | string | no | Human-readable origin |
| `destinationLat` | double | yes | Destination latitude |
| `destinationLon` | double | yes | Destination longitude |
| `destinationAddress` | string | no | Human-readable destination |
| `departureDate` | string | no | Date (YYYY-MM-DD) |
| `seatsNeeded` | int | no | Default: 1 |
| `maxPricePerSeat` | decimal | no | Filter by max price |

## Relevance Scoring

Score = `0.4 × (rating/5) + 0.3 × (1 - normalizedPrice) + 0.3 × (1 - normalizedDistance)`

## External Dependencies

| Service | Used For |
|---------|----------|
| Ride Booking Service | Fetch available rides |
| Review Service | Driver ratings |
| Profile Service | Driver info |
| Auth Service | JWT validation (header forwarding) |

## Running Locally

```bash
cd ride-discovery-service
mvn spring-boot:run
```

Service starts on port **8082** by default.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8082 | Server port |
| `BOOKING_SERVICE_URL` | http://localhost:8083 | Ride Booking Service URL |
| `REVIEW_SERVICE_URL` | http://localhost:8084 | Review Service URL |
| `PROFILE_SERVICE_URL` | http://localhost:8085 | Profile Service URL |

## Running Tests

```bash
mvn test
```

## Notes

- Search results are stored in an in-memory `ConcurrentHashMap` — they are lost on service restart. This is by design for this stateless service.
- If any external service is unavailable, the service degrades gracefully (uses default ratings, empty profiles).
