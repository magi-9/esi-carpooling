# Auth Service

The Auth Service is a core microservice for the Carpool application responsible for handling user registration, authentication, role management, and token validation.

## Features

- **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
- **Role Management:** Support for `DRIVER` and `PASSENGER` roles, allowing role assignment and validation.
- **Event-Driven Architecture:** Publishes events (e.g., `UserRegisteredEvent`) to Apache Kafka when new users register.
- **Stateless Sessions:** JWT-based stateless authentication ensures high scalability.
- **OpenAPI Documentation:** Integrated Swagger UI for easy API exploration and testing.
- **Input Validation:** Stricter security rules with built-in request payload validation.
- **Dockerized Environment:** Provided `compose.yaml` for containerized deployment along with PostgreSQL.

## Tech Stack

- **Java**
- **Spring Boot** (Web, Security, Data JPA, Validation)
- **PostgreSQL 16**
- **Apache Kafka**
- **JSON Web Tokens (jjwt)**
- **Docker & Docker Compose**

## Configuration

The service uses `.env` variables (or standard environment variables) for sensitive configuration. Make sure to provide the following variables before running:

```properties
DB_USER=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET_KEY=your_base64_encoded_secret_key
```

> **Security Note**: The `JWT_SECRET_KEY` is explicitly required to start the application to ensure secure token generation without falling back to insecure hardcoded defaults.

## Endpoints

The API is accessible at `http://localhost:8081`.

### Authentication

- `POST /api/auth/register`: Registers a new user. Expects email, password, and roles. Returns a JWT token.
- `POST /api/auth/login`: Authenticates an existing user and returns a JWT token.
- `POST /api/auth/logout`: Clears the server-side security context.

### Token & Role Validation (For Microservice Intercommunication)

- `GET /api/auth/validate`: Verifies if the provided JWT token in the `Authorization` header is valid.
- `GET /api/auth/validate/role/{role}`: Checks if the currently authenticated user holds a specific role.

### User Role Management

- `GET /api/auth/roles`: Retrieves the roles assigned to the currently authenticated user.
- `PUT /api/auth/roles`: Updates the roles of the currently authenticated user.

*(You can access the full interactive OpenAPI documentation at `http://localhost:8081/swagger-ui.html` once the service is running).*

## Running the Application

### Using Docker Compose

The easiest way to run the service along with its database is via Docker Compose:

```bash
docker compose up --build -d
```

### Running Locally (Development)

To run the application locally (assuming you have an external PostgreSQL and Kafka instance running):

1. Export the required environment variables:

   ```bash
   export DB_USER=your_db_user
   export DB_PASSWORD=your_db_password
   export JWT_SECRET_KEY=your_base64_encoded_secret_key
   ```

2. Run the Spring Boot application via Gradle:

   ```bash
   ./gradlew bootRun
   ```

## Testing

The project includes a suite of integration tests that validate the authentication flows, role assignments, and security constraints. Run them via:

```bash
./gradlew test
```
