# ecommerce-ms — Spring Boot Microservices + Kafka

Learning project: a small e-commerce system split into microservices, using Postgres per service and Kafka for event-driven communication.

## Architecture (current)
- **product-service**: manages products (Postgres)
- **order-service**: creates orders, fetches product pricing, publishes `OrderCreated` to Kafka (Postgres)
- **payment-service**: consumes `OrderCreated`, publishes payment outcome events (mock)

## Services & Ports
| Service | Port | Purpose |
|---|---:|---|
| product-service | 8081 | Products API + Postgres |
| order-service | 8082 | Orders API + Postgres + Kafka producer |
| payment-service | 8083 | Kafka consumer/producer (no REST yet) |
| kafka-ui | 8080 | Kafka UI |

## Tech Stack
- Java 25
- Spring Boot 4
- Postgres
- Kafka (Docker Compose)
- Maven

## Run Kafka locally
From repo root:

```bash
docker compose -f docker-compose.kafka.yml up -d
