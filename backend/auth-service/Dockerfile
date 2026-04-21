# Stage 1: Build the application
# Using Gradle with JDK 25 as specified in your build.gradle
FROM gradle:jdk25 AS build
WORKDIR /app

# Copy the gradle configuration files first to leverage Docker cache
COPY build.gradle settings.gradle ./
COPY src ./src

# Build the application, skipping tests for speed
RUN gradle clean bootJar -x test

# Stage 2: Run the application
# Using a lightweight JRE/JDK image for the runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your service runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]