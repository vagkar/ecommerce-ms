# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Learning project implementing an event-driven e-commerce system with three microservices. Each service is an independent Maven project with its own database.

- **product-service** (port 8081) — product catalog, REST only, PostgreSQL (`productdb`)
- **order-service** (port 8082) — order creation, REST + Kafka producer, PostgreSQL (`orderdb`)
- **payment-service** (port 8083) — mock payment processing, Kafka consumer/producer only (no DB, no REST)

Tech stack: Java 25, Spring Boot 4.0.1, PostgreSQL, Apache Kafka 3.7.0, Maven, Lombok.

## Commands

Each service has its own Maven wrapper. Run commands from the repo root using `-f`:

```bash
# Build a service
./mvnw -f services/product-service clean package
./mvnw -f services/order-service clean package
./mvnw -f services/payment-service clean package

# Run a service
./mvnw -f services/product-service spring-boot:run
./mvnw -f services/order-service spring-boot:run
./mvnw -f services/payment-service spring-boot:run

# Run all tests for a service
./mvnw -f services/order-service test

# Run a single test class
./mvnw -f services/order-service test -Dtest=OrderServiceApplicationTests

# Build without tests
./mvnw -f services/product-service clean package -DskipTests
```

## Infrastructure

Start Kafka and Kafka UI (required before running order-service or payment-service):

```bash
docker compose -f docker-compose.kafka.yml up -d
```

- Kafka broker: `localhost:29092`
- Kafka UI: http://localhost:8080

PostgreSQL is **not** included in Docker Compose — you must run it separately on `localhost:5432`. Required databases: `productdb` (user: `product`, pass: `product`) and `orderdb` (user: `order`, pass: `order`). Hibernate auto-creates the schema on startup (`ddl-auto: update`).

## Architecture

### Event Flow

```
POST /orders (order-service)
  → REST GET /products/{id} (product-service) — fetch price & validate active status
  → Save Order to DB
  → Publish OrderCreatedEvent → Kafka topic: order.events
      → payment-service consumes event
      → Simulates payment (80% success)
      → Publishes PaymentSucceededEvent → payment.succeeded
         OR PaymentFailedEvent → payment.failed
```

Order status update from payment events is **not yet implemented** — the payment outcome is published but nobody consumes it.

### Key Classes

| Service | Class | Role |
|---|---|---|
| order-service | `ProductClient` | `RestClient` wrapper calling product-service |
| order-service | `OrderEventPublisher` | Publishes `OrderCreatedEvent` to `order.events` |
| payment-service | `OrderEventsListener` | `@KafkaListener` on `order.events`, consumer group `payment-service` |
| payment-service | `PaymentEventPublisher` | Publishes succeeded/failed events |

### Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.events` | order-service | payment-service |
| `payment.succeeded` | payment-service | _(none yet)_ |
| `payment.failed` | payment-service | _(none yet)_ |

### Serialization

All Kafka messages use Jackson JSON. Payment-service deserializes with a fixed default type (`spring.json.value.default.type: com.ecommerce.payment.messaging.OrderCreatedEvent`) and `spring.json.use.type.headers: false` — no type headers in the message. Trusted packages: `com.ecommerce`.

## Package Structure

All services follow the same base package: `com.ecommerce.<service-name>`.
