# ESI2026 Carpooling Platform

Microservices-based carpooling system built for the Enterprise System Integration course (UT 2026).

## Services

| Service | Owner | Port | Description |
|---------|-------|------|-------------|
| ride-discovery-service | Tomáš | 8082 | Search & rank rides |
| payment-service | Tomáš | 8081 | Payment lifecycle |
| ride-booking-service | Paulius | 8083 | Create/book rides |
| review-service | Paulius | 8084 | Ratings & reviews |
| profile-service | Simon | 8085 | User profiles |
| auth-service | Simon | 8086 | Authentication & JWT |
| validation-service | Renan | 8087 | Document validation |
| geolocation-service | Renan | 8088 | Geocoding |

## Running Locally (Docker Compose)

```bash
cp .env.example .env
# Edit .env with your settings
docker-compose up --build
```

Frontend: http://localhost:3000
Payment Service: http://localhost:8081
Ride Discovery Service: http://localhost:8082

## Running Services Individually

```bash
# Payment Service
cd payment-service && mvn spring-boot:run

# Ride Discovery Service
cd ride-discovery-service && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```
