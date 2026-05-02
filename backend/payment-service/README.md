# Payment Service

Manages the complete payment lifecycle after a ride is completed, including transaction authorization and refunds.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/payments` | Initiate a payment for a completed ride |
| GET | `/payments/{paymentId}` | Get payment details |
| POST | `/payments/{paymentId}/refunds` | Request a refund for a completed payment |
| GET | `/payments/{paymentId}/refunds` | Get refund details |
| POST | `/payments/authorize` | Authorize a transaction and place a temporary hold |

## Payment State Machine

```
INITIATED ──► PROCESSING ──► COMPLETED ──► REFUNDED
    │               │
    └───────────────┴──► FAILED
```

- `POST /payments` creates a completed payment record for the approved flow.
- `POST /payments/authorize` creates a processing payment that can be captured later.
- Only `COMPLETED` payments can be refunded.

## Domain Model

- **Payment** (Aggregate Root) — owns the full lifecycle
- **Money** (Value Object) — amount + currency, immutable
- **Refund** (Entity) — created when payment is refunded, has its own identity
- **PaymentRepository** (Interface) — abstraction over JPA persistence

## Running Locally

```bash
cd payment-service
mvn spring-boot:run
```

Service starts on port **8081** by default and uses an H2 in-memory database.

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

## Implementation Notes

- The service persists payments in its own database through JPA.
- The controller currently exposes initiate, authorize, get-payment, refund-create, and refund-get routes.
