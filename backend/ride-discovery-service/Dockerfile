# --- Stage 1: Build the application ---
# Use a Maven image with Java 21 to build the artifact
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (caching step)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build (including tests)
COPY src ./src
RUN mvn clean package

# --- Stage 2: Run the application ---
# Use a lightweight JRE image for the final container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
