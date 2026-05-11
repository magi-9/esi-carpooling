# Docker runs instructions in a Dockerfile in order. 
# A Dockerfile must begin with a FROM instruction. 
# ARG is the only instruction that may precede FROM in the Dockerfile. 
# The FROM instruction specifies the Parent Image from which you are building.

# FROM [--platform=<platform>] <image>[:<tag>] [AS <name>]
FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app

# Copy pom.xml first and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Set Maven to use local repository
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

ENTRYPOINT ["mvn", "clean", "spring-boot:run"]

EXPOSE 8080