FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

ENTRYPOINT ["mvn", "clean", "spring-boot:run"]

EXPOSE 8080
