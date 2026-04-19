# Payment Service

Handles the full payment lifecycle for completed rides — from initiation through completion and optional refund.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/payments` | Initiate a new payment |
| GET | `/payments/{paymentId}` | Get payment details |
| PATCH | `/payments/{paymentId}/complete` | Mark payment as completed |
| POST | `/payments/{paymentId}/refunds` | Request a refund |
| GET | `/payments/{paymentId}/refunds` | Get refund status |

## Payment State Machine

```
INITIATED ──► PROCESSING ──► COMPLETED ──► REFUNDED
    │               │
    └───────────────┴──► FAILED
```

- Only `INITIATED` / `PROCESSING` can be completed or failed
- Only `COMPLETED` can be refunded

## Domain Model

- **Payment** (Aggregate Root) — owns the full lifecycle
- **Money** (Value Object) — amount + currency, immutable
- **Refund** (Entity) — created when payment is refunded, has own identity
- **PaymentRepository** (Interface) — abstraction over JPA persistence

## Running Locally

```bash
cd payment-service
mvn spring-boot:run
```

Service starts on port **8081** (H2 in-memory DB).

H2 Console: http://localhost:8081/h2-console
JDBC URL: `jdbc:h2:mem:paymentdb`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8081 | Server port |
| `DB_URL` | H2 in-memory | JDBC URL |
| `DB_USERNAME` | sa | DB username |
| `DB_PASSWORD` | (empty) | DB password |
| `DB_DRIVER` | org.h2.Driver | JDBC driver |
| `JPA_DIALECT` | H2Dialect | Hibernate dialect |
| `BOOKING_SERVICE_URL` | http://localhost:8083 | Ride Booking Service URL |

## Running Tests

```bash
mvn test
```

## Production (Postgres)

Set these env vars:
```
DB_URL=jdbc:postgresql://host:5432/paymentdb
DB_USERNAME=postgres
DB_PASSWORD=secret
DB_DRIVER=org.postgresql.Driver
JPA_DIALECT=org.hibernate.dialect.PostgreSQLDialect
```
