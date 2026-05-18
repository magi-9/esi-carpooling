# Ride Discovery Service

Search engine for passengers that finds, filters, and ranks available rides.

## Overview

This service is stateless and does **not** own a database. It aggregates data from the Geolocation, Ride Booking, and Review services, then ranks the matching rides by relevance.

This is intentionally an aggregation/search service: it keeps the layered controller/service/domain/client structure, but it does not persist search history because searches are request-scoped.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/search` | Search rides with criteria. Query params: `originAddress`, `destinationAddress`, `originLat`, `originLon`, `destinationLat`, `destinationLon`, `departureDate`, `seatsNeeded`, `maxPricePerSeat`. Either an address pair or a coordinate pair must be provided. |

### Search Query Parameters

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `originLat` | double | no | Origin latitude when using coordinates |
| `originLon` | double | no | Origin longitude when using coordinates |
| `originAddress` | string | no | Human-readable origin when using addresses |
| `destinationLat` | double | no | Destination latitude when using coordinates |
| `destinationLon` | double | no | Destination longitude when using coordinates |
| `destinationAddress` | string | no | Human-readable destination when using addresses |
| `departureDate` | string | no | Date in `YYYY-MM-DD` format |
| `seatsNeeded` | int | no | Default: 1 |
| `maxPricePerSeat` | decimal | no | Optional maximum price per seat; forwarded to Ride Booking and included in the returned criteria |

## Relevance Scoring

Score = `0.4 × (rating/5) + 0.3 × (1 - normalizedPrice) + 0.3 × (1 - normalizedDistance)`

## External Dependencies

| Service | Used For |
|---------|----------|
| Geolocation Service | Convert addresses to coordinates |
| Ride Booking Service | Fetch available rides |
| Review Service | Driver ratings |

## Running Locally

```bash
cd backend/ride-discovery-service
mvn spring-boot:run
```

Service starts on port **8082** by default.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8082 | Server port |
| `BOOKING_SERVICE_URL` | http://localhost:8083 | Ride Booking Service URL |
| `REVIEW_SERVICE_URL` | http://localhost:8084 | Review Service URL |
| `GEOLOCATION_SERVICE_URL` | http://localhost:8088 | Geolocation Service URL |

## Running Tests

```bash
mvn test
```

## Running with Docker Compose

From the repository root, run the service together with the shared project network:

```bash
docker compose up --build ride-discovery-service
```

The service-level compose file is also valid by itself:

```bash
cd backend/ride-discovery-service
docker compose up --build
```

## Notes

- Search results are returned directly from the request flow; this service does not persist a search history.
- If an address is provided, the service resolves it through Geolocation before querying the Ride Booking service.
- Ride Booking and Review failures degrade gracefully where possible, for example empty ride lists or default ratings. Address search still requires Geolocation to resolve the provided address.
