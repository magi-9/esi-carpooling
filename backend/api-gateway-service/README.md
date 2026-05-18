# API Gateway Service

Spring Cloud Gateway entry point for the ESI Carpooling backend.

## Scope

This service centralizes frontend-facing routing under `/api` and forwards requests to backend services:

| Gateway path | Target service |
|--------------|----------------|
| `/api/auth/**` | Auth Service |
| `/api/profiles/**` | Profile Service |
| `/api/rides/**` | Ride Booking Service |
| `/api/bookings/**` | Ride Booking Service |
| `/api/payments`, `/api/payments/**` | Payment Service |
| `/api/search/**` | Ride Discovery Service |
| `/api/reviews/**` | Review Service |
| `/api/geocode`, `/api/reverse-geocode` | Geolocation Service |
| `/api/validation/**` | Validation Service |

No persistence and no business logic are implemented here.

## Run tests

```bash
mvn test
```

## Run locally

```bash
mvn spring-boot:run
```

Gateway listens on port `8080` by default.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build api-gateway-service
```

The service-level compose file is valid for starting the gateway container by itself, but real routing requires the target services to be running on the same `esi-network`.
