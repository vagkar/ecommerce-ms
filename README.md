# ecommerce-ms — Spring Boot Microservices + Kafka

Full-stack e-commerce system built as a portfolio project. Event-driven architecture with four Spring Boot microservices communicating via Kafka, JWT authentication, and a Vue.js frontend (in progress).

## Architecture

```
                    Vue.js Frontend (port 5173)
                 /           |              \
   login/register    Bearer token      Bearer token
               /             |                \
              ▼              ▼                 ▼
┌──────────────────┐  ┌───────────────┐  ┌────────────────┐
│  user-service    │  │ order-service │  │product-service │
│  (port 8084)     │  │  (port 8082)  │  │  (port 8081)   │
└──────────────────┘  └───────────────┘  └────────────────┘
  issues JWT token            │   REST GET /products/{id}  ▲
                              │ ──────────────────────────►│
                              │ Kafka: order.events
                              ▼
                    ┌───────────────────┐
                    │  payment-service  │
                    │    (port 8083)    │
                    └───────────────────┘
                              │
                 ─────────────┴─────────────
                 │                          │
      Kafka: payment.succeeded    Kafka: payment.failed
                 └──────────────┬───────────┘
                                ▼
                       order-service updates
                       order status (PAID /
                       PAYMENT_FAILED)
```

### Event Flow

1. `POST /auth/login` — user-service validates credentials, returns JWT token
2. `POST /orders` — client sends JWT in `Authorization` header
3. order-service validates the token and extracts `userId`
4. order-service validates product via REST call to product-service
5. Order saved to DB with status `CREATED`
6. `OrderCreatedEvent` published to Kafka topic `order.events`
7. payment-service consumes the event, simulates payment (80% success)
8. `PaymentSucceededEvent` or `PaymentFailedEvent` published to Kafka
9. order-service consumes the payment result and updates order status to `PAID` or `PAYMENT_FAILED`
10. order-service broadcasts status via WebSocket to `/topic/orders/{orderId}`
11. Vue.js frontend receives live update via STOMP subscription

## Services & Ports

| Service         | Port | Purpose                                           |
|----------------|-----:|---------------------------------------------------|
| product-service | 8081 | Products REST API + PostgreSQL                    |
| order-service   | 8082 | Orders REST API + PostgreSQL + Kafka              |
| payment-service | 8083 | Kafka consumer/producer — mock payments           |
| user-service    | 8084 | Authentication — register, login, JWT issuance    |
| kafka-ui        | 8080 | Kafka UI dashboard                                |

## Tech Stack

- **Backend:** Java 25, Spring Boot 4.0.1, Spring Data JPA, Spring Security, Spring Kafka
- **Auth:** JWT (jjwt 0.12.6), BCrypt password hashing
- **Frontend:** Vue 3, TypeScript, Vite, Pinia, Vue Router, Axios, @stomp/stompjs
- **Database:** PostgreSQL (one DB per service)
- **Messaging:** Apache Kafka 3.7.0
- **Infrastructure:** Docker Compose

## API Endpoints

### user-service (port 8084) — public

| Method | Path             | Description                        |
|--------|------------------|------------------------------------|
| `POST` | `/auth/register` | Register — returns JWT token       |
| `POST` | `/auth/login`    | Login — returns JWT token          |

### product-service (port 8081)

| Method  | Path                        | Auth     | Description           |
|---------|-----------------------------|----------|-----------------------|
| `GET`   | `/products`                 | public   | List all products     |
| `GET`   | `/products/{id}`            | public   | Get product by ID     |
| `POST`  | `/products`                 | required | Create product        |
| `PUT`   | `/products/{id}`            | required | Update name and price |
| `PATCH` | `/products/{id}/deactivate` | required | Deactivate product    |

### order-service (port 8082) — all endpoints require JWT

| Method | Path           | Description                        |
|--------|----------------|------------------------------------|
| `POST` | `/orders`      | Create order (userId from token)   |
| `GET`  | `/orders/{id}` | Get order by ID                    |
| `GET`  | `/orders`      | List own orders (paginated)        |

## Getting Started

### Prerequisites

- Java 25
- Maven
- Docker (for Kafka)
- PostgreSQL running on `localhost:5432`

### Databases

Create the following PostgreSQL databases before starting:

| Database    | User      | Password  |
|-------------|-----------|-----------|
| `productdb` | `product` | `product` |
| `orderdb`   | `order`   | `order`   |
| `userdb`    | `user`    | `user`    |

Hibernate auto-creates the schema on first startup (`ddl-auto: update`).

### 1. Start Kafka

```bash
docker compose -f docker-compose.kafka.yml up -d
```

Kafka UI available at http://localhost:8080

### 2. Start Services

```bash
# In separate terminals
./mvnw -f services/user-service spring-boot:run
./mvnw -f services/product-service spring-boot:run
./mvnw -f services/order-service spring-boot:run
./mvnw -f services/payment-service spring-boot:run
```

### 3. Try It Out

```bash
# Register
curl -X POST http://localhost:8084/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'

# Login (keep the token from the response)
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'

# Create a product (requires token)
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"sku":"LAPTOP-01","name":"Laptop Pro","price":999.99}'

# Create an order (replace <product-id> with the id returned above)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"items":[{"productId":"<product-id>","quantity":1}]}'

# After ~1-2 seconds, check the order status
curl http://localhost:8082/orders/<order-id> \
  -H "Authorization: Bearer <token>"
# "status" will be "PAID" or "PAYMENT_FAILED"
```

## Configuration

All sensitive values are configured via environment variables with local defaults:

| Variable                  | Default                        | Used by                    |
|---------------------------|--------------------------------|----------------------------|
| `DB_URL`                  | service-specific localhost URL | product, order, user       |
| `DB_USERNAME`             | service-specific username      | product, order, user       |
| `DB_PASSWORD`             | service-specific password      | product, order, user       |
| `JWT_SECRET`              | development key                | product, order, user       |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092`              | order, payment             |
| `PRODUCT_SERVICE_URL`     | `http://localhost:8081`        | order                      |
| `JWT_EXPIRATION_MS`       | `86400000` (24h)               | user                       |

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
- [x] JWT authentication — user-service with register/login
- [x] JWT validation filter in order-service and product-service
- [x] Environment variable configuration
- [x] Vue.js frontend (products, cart, orders, live order status via WebSocket)
- [x] WebSocket real-time order status updates (STOMP `/ws`, broadcasts to `/topic/orders/{id}`)
- [x] Product names stored in order items (snapshot at order time)
- [x] Login redirect to intended page after authentication
- [x] Cart persistence via localStorage
- [x] WebSocket auto-reconnect with connection indicator
- [ ] Pagination UI for orders list
- [ ] Full Docker Compose (all services + frontend)
- [ ] Tests (unit, web layer, Kafka integration)