# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :modules:tenants:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/modules/tenants/build/libs/*SNAPSHOT.jar app.jar
# Default ports used by app (can be overridden by env)
EXPOSE 8081 9091
ENV TENANTS_HTTP_PORT=8081 \
    TENANTS_GRPC_PORT=9091 \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=guest \
    RABBITMQ_PASSWORD=guest \
    RABBITMQ_VHOST=/ \
    EUREKA_URL=http://eureka-server:8761/eureka/ \
    EUREKA_REGISTER=true \
    EUREKA_FETCH=true \
    EVENTS_EXCHANGE_PREFIX=events.v1. \
    EVENTS_ROUTING_KEY=events
ENTRYPOINT ["java","-jar","/app/app.jar"]
