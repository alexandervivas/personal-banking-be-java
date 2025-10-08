# C4 — Containers

```mermaid
flowchart TB
  subgraph External
    FE[Next.js App Router]
    Partners[Partner Integrations]
  end

  subgraph API[API Module]
    REST[REST Controllers / Security Proxy]
    Ingress[AMQP Consumers / Publishers]
  end

  subgraph Modules[Business Modules (gRPC endpoints + RabbitMQ events)]
    Tenants[Tenants Module]
    Core[Other Domain Modules]
  end

  subgraph Infra[Infrastructure]
    PG[(PostgreSQL)]
    RMQ[(RabbitMQ)]
    FX[FX Provider SPI]
  end

  FE -->|HTTPS| REST
  Partners -->|AMQP| Ingress
  REST -->|gRPC| Tenants
  REST -->|gRPC| Core
  Ingress -->|gRPC| Tenants
  Ingress -->|gRPC| Core
  Tenants -->|JDBC| PG
  Tenants -->|AMQP events| RMQ
  Core -->|AMQP events| RMQ
  RMQ -->|AMQP events| Tenants
  RMQ -->|AMQP events| Core
  Core -->|HTTPS| FX
  REST --> OTel[OTel SDK] --> Prom[Prometheus] --> Graf[Grafana]
```
