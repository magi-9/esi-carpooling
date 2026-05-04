# Docker runs instructions in a Dockerfile in order. 
# A Dockerfile must begin with a FROM instruction. 
# ARG is the only instruction that may precede FROM in the Dockerfile. 
# The FROM instruction specifies the Parent Image from which you are building.

# FROM [--platform=<platform>] <image>[:<tag>] [AS <name>]
FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
ENTRYPOINT ["mvn", "spring-boot:run"]

EXPOSE 8080