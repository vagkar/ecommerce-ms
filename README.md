# ecommerce-ms — Spring Boot Microservices + Kafka

Full-stack e-commerce system built as a portfolio project. Event-driven architecture with three Spring Boot microservices communicating via Kafka, and a Vue.js frontend (in progress).

## Architecture

```
Vue.js Frontend (port 5173)
        │
        ▼
┌───────────────────┐     REST      ┌────────────────────┐
│   order-service   │ ────────────► │  product-service   │
│     (port 8082)   │               │    (port 8081)     │
└───────────────────┘               └────────────────────┘
        │
        │ Kafka: order.events
        ▼
┌───────────────────┐
│  payment-service  │
│    (port 8083)    │
└───────────────────┘
        │
        ├── Kafka: payment.succeeded ──┐
        └── Kafka: payment.failed ─────┤
                                       ▼
                              order-service updates
                              order status (PAID /
                              PAYMENT_FAILED)
```

### Event Flow

1. `POST /orders` — order-service validates product via REST call to product-service
2. Order saved to DB with status `CREATED`
3. `OrderCreatedEvent` published to Kafka topic `order.events`
4. payment-service consumes the event, simulates payment (80% success)
5. `PaymentSucceededEvent` or `PaymentFailedEvent` published to Kafka
6. order-service consumes the payment result and updates order status to `PAID` or `PAYMENT_FAILED`

## Services & Ports

| Service         | Port | Purpose                                      |
|----------------|-----:|----------------------------------------------|
| product-service | 8081 | Products REST API + PostgreSQL               |
| order-service   | 8082 | Orders REST API + PostgreSQL + Kafka         |
| payment-service | 8083 | Kafka consumer/producer — mock payments      |
| kafka-ui        | 8080 | Kafka UI dashboard                           |

## Tech Stack

- **Backend:** Java 25, Spring Boot 4.0.1, Spring Data JPA, Spring Kafka
- **Frontend:** Vue 3, TypeScript, Vite, Pinia, Vue Router, Tailwind CSS, PrimeVue _(in progress)_
- **Database:** PostgreSQL (one DB per service)
- **Messaging:** Apache Kafka 3.7.0
- **Infrastructure:** Docker Compose

## API Endpoints

### product-service (port 8081)

| Method  | Path                        | Description              |
|---------|-----------------------------|--------------------------|
| `GET`   | `/products`                 | List all products        |
| `GET`   | `/products/{id}`            | Get product by ID        |
| `POST`  | `/products`                 | Create product           |
| `PUT`   | `/products/{id}`            | Update name and price    |
| `PATCH` | `/products/{id}/deactivate` | Deactivate product       |

### order-service (port 8082)

| Method | Path              | Description                        |
|--------|-------------------|------------------------------------|
| `GET`  | `/orders/{id}`    | Get order by ID                    |
| `GET`  | `/orders?userId=` | List orders for a user (paginated) |
| `POST` | `/orders`         | Create order                       |

## Getting Started

### Prerequisites

- Java 25
- Maven
- Docker (for Kafka)
- PostgreSQL running on `localhost:5432`

### Databases

Create the following PostgreSQL databases before starting:

| Database   | User      | Password  |
|------------|-----------|-----------|
| `productdb` | `product` | `product` |
| `orderdb`   | `order`   | `order`   |

Hibernate auto-creates the schema on first startup (`ddl-auto: update`).

### 1. Start Kafka

```bash
docker compose -f docker-compose.kafka.yml up -d
```

Kafka UI available at http://localhost:8080

### 2. Start Services

```bash
# In separate terminals
./mvnw -f services/product-service spring-boot:run
./mvnw -f services/order-service spring-boot:run
./mvnw -f services/payment-service spring-boot:run
```

### 3. Try It Out

```bash
# Create a product
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"LAPTOP-01","name":"Laptop Pro","price":999.99}'

# Create an order (replace <product-id> with the id returned above)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"00000000-0000-0000-0000-000000000001","items":[{"productId":"<product-id>","quantity":1}]}'

# After ~1-2 seconds, check the order status
curl http://localhost:8082/orders/<order-id>
# "status" will be "PAID" or "PAYMENT_FAILED"
```

## Build Commands

```bash
# Build a service
./mvnw -f services/product-service clean package

# Run tests
./mvnw -f services/order-service test

# Build without tests
./mvnw -f services/product-service clean package -DskipTests
```

## Roadmap

- [x] Product catalog REST API
- [x] Order creation with Kafka event publishing
- [x] Mock payment processing via Kafka
- [x] Payment feedback loop — order status updated from payment events
- [x] Global exception handling with proper HTTP status codes
- [x] CORS configuration for Vue frontend
- [ ] Vue.js frontend (products, cart, orders)
- [ ] JWT authentication (user-service)
- [ ] WebSocket real-time order status updates
- [ ] Full Docker Compose (all services + frontend)
- [ ] Tests (unit, web layer, Kafka integration)