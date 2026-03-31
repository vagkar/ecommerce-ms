# ecommerce-ms — Event-Driven Microservices

A full-stack e-commerce system built as a portfolio project. Four Spring Boot microservices communicate asynchronously via Apache Kafka, secured with JWT authentication, and served through a Vue.js frontend.

## Architecture

```
                        ┌─────────────────────────────┐
                        │      Vue.js Frontend         │
                        │   nginx (port 80)            │
                        │   /api/* → backend services  │
                        └──────────┬──────────────────┘
                                   │ JWT
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                     ▼
   ┌──────────────────┐  ┌────────────────┐  ┌──────────────────┐
   │  user-service    │  │ order-service  │  │ product-service  │
   │  (port 8084)     │  │  (port 8082)   │  │  (port 8081)     │
   │  PostgreSQL      │  │  PostgreSQL    │  │  PostgreSQL      │
   └──────────────────┘  └───────┬────────┘  └──────────────────┘
     issues JWT tokens           │ REST GET /products/{id}   ▲
                                 │ ─────────────────────────►│
                                 │ Kafka: order.events
                                 ▼
                       ┌───────────────────┐
                       │  payment-service  │
                       │  (port 8083)      │
                       │  Kafka only       │
                       └─────────┬─────────┘
                                 │
                  ───────────────┴───────────────
                  │                              │
       Kafka: payment.succeeded       Kafka: payment.failed
                  └──────────────┬───────────────┘
                                 ▼
                    order-service updates status
                    → WebSocket broadcast to frontend
```

### Event Flow

1. `POST /auth/login` — user-service returns JWT token
2. `POST /orders` — order-service validates JWT, extracts `userId`
3. order-service calls product-service via REST to validate product and fetch price
4. Order saved with status `CREATED`, `OrderCreatedEvent` published to Kafka
5. payment-service consumes event, simulates payment (80% success rate)
6. `PaymentSucceededEvent` or `PaymentFailedEvent` published to Kafka
7. order-service consumes result, updates order to `PAID` or `PAYMENT_FAILED`
8. order-service broadcasts new status via WebSocket → frontend updates live

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 25, Spring Boot 4.0.1, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6), BCrypt |
| Messaging | Apache Kafka 3.7.0 |
| Database | PostgreSQL (one database per service) |
| Frontend | Vue 3, TypeScript, Vite, Pinia, Vue Router, Axios |
| Real-time | WebSocket / STOMP |
| Infrastructure | Docker, nginx |
| Testing | JUnit 5, Mockito, MockMvc, EmbeddedKafka |

## Quick Start (Docker)

```bash
docker compose up --build
```

- Frontend: http://localhost
- Kafka UI: http://localhost:8080

That's it. Docker starts PostgreSQL, Kafka, all four services, and the frontend.

To stop:
```bash
docker compose down        # stop
docker compose down -v     # stop + delete database volumes
```

## Local Development

Requires: Java 25, Maven, Docker, PostgreSQL on `localhost:5432`

### 1. Create databases

| Database    | User      | Password  |
|-------------|-----------|-----------|
| `productdb` | `product` | `product` |
| `orderdb`   | `order`   | `order`   |
| `userdb`    | `user`    | `user`    |

### 2. Start Kafka

```bash
docker compose -f docker-compose.kafka.yml up -d
```

### 3. Start services

```bash
./mvnw -f services/user-service spring-boot:run
./mvnw -f services/product-service spring-boot:run
./mvnw -f services/order-service spring-boot:run
./mvnw -f services/payment-service spring-boot:run
```

### 4. Start frontend

```bash
cd frontend
npm install
npm run dev    # http://localhost:5173
```

## API Reference

### user-service — public

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Register — returns JWT |
| `POST` | `/auth/login` | Login — returns JWT |

### product-service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/products` | — | List all active products |
| `GET` | `/products/{id}` | — | Get product by ID |
| `POST` | `/products` | required | Create product |
| `PUT` | `/products/{id}` | required | Update product |
| `PATCH` | `/products/{id}/deactivate` | required | Deactivate product |

### order-service — all endpoints require JWT

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/orders` | Create order (userId extracted from token) |
| `GET` | `/orders` | List own orders (paginated, `?page=0&size=20`) |
| `GET` | `/orders/{id}` | Get order by ID |
| `GET` | `/cart` | Get current user's cart |
| `PUT` | `/cart/items` | Add or update item in cart |
| `DELETE` | `/cart/items/{productId}` | Remove item from cart |
| `DELETE` | `/cart` | Clear cart |

## Tests

```bash
# All tests for a service
./mvnw -f services/order-service test

# Unit tests only (fast)
./mvnw -f services/order-service test -Dtest="OrderServiceTest,CartServiceTest"

# Single test class
./mvnw -f services/order-service test -Dtest=OrderServiceTest
```

| Type | Class | What it tests |
|------|-------|---------------|
| Unit | `OrderServiceTest` | Order creation, product validation, event publishing |
| Unit | `CartServiceTest` | Cart CRUD, add/update/remove items |
| Web layer | `OrderControllerTest` | HTTP routing, JWT auth, request/response format |
| Web layer | `CartControllerTest` | HTTP routing, JWT auth, cart endpoints |
| Integration | `OrderEventPublisherIntegrationTest` | Full Kafka flow with EmbeddedKafka + H2 |

## Configuration

All sensitive values use environment variables with local defaults:

| Variable | Default | Services |
|----------|---------|----------|
| `JWT_SECRET` | development key | product, order, user |
| `DB_URL` | `jdbc:postgresql://localhost:5432/<db>` | product, order, user |
| `DB_USERNAME` | service-specific | product, order, user |
| `DB_PASSWORD` | service-specific | product, order, user |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092` | order, payment |
| `PRODUCT_SERVICE_URL` | `http://localhost:8081` | order |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | user |

## Roadmap

- [x] Product catalog REST API
- [x] Order creation with Kafka event publishing
- [x] Mock payment processing via Kafka
- [x] Payment feedback loop — order status updated from Kafka events
- [x] Global exception handling with proper HTTP status codes
- [x] JWT authentication — user-service issues tokens, other services validate
- [x] Environment variable configuration
- [x] Vue.js frontend — products, cart, orders, live status via WebSocket
- [x] WebSocket real-time order status (STOMP, auto-reconnect)
- [x] Login redirect to intended page after authentication
- [x] Cart — localStorage persistence + backend API sync (local-first)
- [x] Pagination UI for orders list
- [x] Tests — unit (Mockito), web layer (MockMvc), integration (EmbeddedKafka)
- [x] Docker Compose — all services + Vue frontend via nginx reverse proxy
