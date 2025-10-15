# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
# Copy entire repository as context is set to repo root in compose
COPY . .
# Build only the eureka-server module
RUN ./gradlew :modules:eureka-server:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/modules/eureka-server/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
