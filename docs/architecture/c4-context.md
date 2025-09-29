# C4 — System Context


```mermaid
flowchart LR
User[End User] -->|Web| NextJS[Next.js Frontend]
NextJS -->|REST /v1| API[Spring Boot API]
API -->|JDBC| PG[(PostgreSQL)]
API -->|AMQP| RMQ[(RabbitMQ)]
API -->|OTEL| OTel[OTel Collector]
OTel --> Prom[Prometheus]
Prom --> Graf[Grafana]

```
