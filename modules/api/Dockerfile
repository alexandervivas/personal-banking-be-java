# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :modules:api:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/modules/api/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENV API_HTTP_PORT=8080 \
    EUREKA_URL=http://eureka-server:8761/eureka/ \
    EUREKA_REGISTER=false \
    EUREKA_FETCH=true
ENTRYPOINT ["java","-jar","/app/app.jar"]
