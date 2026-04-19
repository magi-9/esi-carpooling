# API Gateway Service

Minimal API gateway for the ESI project.

## Scope

This service routes only to currently implemented backend services:

- `GET/POST/PATCH /api/payments/**` -> Payment Service
- `GET /api/search/**` -> Ride Discovery Service

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
