# C4 — System Context

```mermaid
flowchart LR
  User[End User] -->|Web| NextJS[Next.js Frontend]
  Partner[Partner Systems] -->|AMQP| API[API Module]
  NextJS -->|HTTPS /v1| API
  API -->|gRPC| Modules[Business Modules (API RPC only)]
  Modules -->|JDBC| PG[(PostgreSQL)]
  Modules -->|AMQP events| RMQ[(RabbitMQ)]
  RMQ -->|AMQP events| Modules
  API -->|OTEL| OTel[OTel Collector]
  OTel --> Prom[Prometheus]
  Prom --> Graf[Grafana]
```
