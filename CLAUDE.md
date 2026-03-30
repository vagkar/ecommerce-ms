# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Portfolio/learning project implementing an event-driven e-commerce system with four microservices. Each service is an independent Maven project with its own database.

- **product-service** (port 8081) — product catalog, REST + JWT auth, PostgreSQL (`productdb`)
- **order-service** (port 8082) — order creation, REST + JWT auth + Kafka producer, PostgreSQL (`orderdb`)
- **payment-service** (port 8083) — mock payment processing, Kafka consumer/producer only (no DB, no REST)
- **user-service** (port 8084) — JWT authentication, register/login, PostgreSQL (`userdb`)

Tech stack: Java 25, Spring Boot 4.0.1, Spring Security, PostgreSQL, Apache Kafka 3.7.0, Maven, Lombok, jjwt 0.12.6, Vue 3, TypeScript, Vite, Pinia, Vue Router, Axios.

## Commands

Each service has its own Maven wrapper. Run commands from the repo root using `-f`:

```bash
# Build a service
./mvnw -f services/product-service clean package
./mvnw -f services/order-service clean package
./mvnw -f services/payment-service clean package
./mvnw -f services/user-service clean package

# Run a service
./mvnw -f services/product-service spring-boot:run
./mvnw -f services/order-service spring-boot:run
./mvnw -f services/payment-service spring-boot:run
./mvnw -f services/user-service spring-boot:run

# Run all tests for a service
./mvnw -f services/order-service test

# Run a single test class
./mvnw -f services/order-service test -Dtest=OrderServiceTest

# Run all unit tests only (fast)
./mvnw -f services/order-service test -Dtest="OrderServiceTest,CartServiceTest"

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

PostgreSQL is **not** included in Docker Compose — you must run it separately on `localhost:5432`. Required databases:
- `productdb` (user: `product`, pass: `product`)
- `orderdb` (user: `order`, pass: `order`)
- `userdb` (user: `user`, pass: `user`)

Hibernate auto-creates the schema on startup (`ddl-auto: update`).

## Architecture

### Event Flow

```
POST /auth/login (user-service) → returns JWT token
POST /orders (order-service) — requires JWT in Authorization header
  → Extracts userId from JWT token
  → REST GET /products/{id} (product-service) — fetch price & validate active status
  → Save Order to DB (status: CREATED)
  → Publish OrderCreatedEvent → Kafka topic: order.events
      → payment-service consumes event
      → Simulates payment (80% success)
      → Publishes PaymentSucceededEvent → payment.succeeded
         OR PaymentFailedEvent → payment.failed
      → order-service consumes result → updates order status (PAID / PAYMENT_FAILED)
      → order-service broadcasts status via WebSocket → /topic/orders/{orderId}
```

### Authentication Flow

- `user-service` issues JWT tokens on register/login
- JWT contains: `sub` (userId), `email`, `role` (CUSTOMER/ADMIN)
- `order-service` and `product-service` validate JWT via `JwtAuthFilter`
- Both services share the same `JWT_SECRET` environment variable
- `userId` is extracted from the token — never sent in the request body

### Access Control

| Endpoint | Auth required |
|---|---|
| `GET /products`, `GET /products/{id}` | No |
| `POST/PUT/PATCH /products/**` | Yes |
| All `/orders/**` | Yes |
| All `/cart/**` | Yes |
| `POST /auth/register`, `POST /auth/login` | No |

### Key Classes

| Service | Class | Role |
|---|---|---|
| user-service | `JwtService` | Generates JWT tokens |
| user-service | `UserService` | Register/login with BCrypt |
| order-service | `JwtAuthFilter` | Validates JWT on every request |
| order-service | `ProductClient` | `RestClient` wrapper calling product-service |
| order-service | `OrderEventPublisher` | Publishes `OrderCreatedEvent` to `order.events` |
| order-service | `PaymentEventsListener` | `@KafkaListener` on `payment.succeeded` + `payment.failed`, triggers WebSocket broadcast |
| order-service | `WebSocketConfig` | STOMP endpoint `/ws`, topic prefix `/topic` |
| order-service | `OrderStatusBroadcaster` | Broadcasts order status to `/topic/orders/{orderId}` via `SimpMessagingTemplate` |
| order-service | `CartController` | REST API for cart CRUD (`/cart/**`) |
| order-service | `CartService` | Cart business logic with getOrCreate pattern |
| payment-service | `OrderEventsListener` | `@KafkaListener` on `order.events`, consumer group `payment-service` |
| payment-service | `PaymentEventPublisher` | Publishes succeeded/failed events |
| product-service | `JwtAuthFilter` | Validates JWT on every request |

### Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.events` | order-service | payment-service |
| `payment.succeeded` | payment-service | order-service |
| `payment.failed` | payment-service | order-service |

### Serialization

All Kafka messages use Jackson JSON. Payment-service deserializes with a fixed default type (`spring.json.value.default.type: com.ecommerce.payment.messaging.OrderCreatedEvent`) and `spring.json.use.type.headers: false` — no type headers in the message. Trusted packages: `com.ecommerce`.

Order-service uses `spring.json.type.mapping` to map payment-service event types to its own local records.

## Configuration

All sensitive values use environment variables with local defaults (`${VAR:default}`). Never hardcode credentials. See README for the full list of variables.

## Package Structure

All services follow the same base package: `com.ecommerce.<service-name>`.

Standard packages per service:
- `controller` — REST endpoints
- `service` — business logic
- `repository` — JPA repositories
- `entity` — JPA entities
- `dto` — request/response records
- `messaging` — Kafka producers/listeners + event records
- `config` — Spring configuration (Security, CORS, etc.)
- `security` — JWT filter
- `exception` — custom exceptions + global handler
- `websocket` — WebSocket config + broadcaster (order-service only)

## Frontend (`frontend/`)

Vue 3 + TypeScript SPA. Dev server: `http://localhost:5173`.

### Commands

```bash
cd frontend
npm install
npm run dev       # start dev server
npm run lint      # lint + fix
npm run build     # production build
```

### Frontend Structure

```
src/
  api/          — Axios HTTP clients (one per backend service)
  stores/       — Pinia state management (auth, cart, order)
  composables/  — Reusable logic (useOrderStatus for WebSocket)
  views/        — Page components (one per route)
  components/   — Reusable UI components
  router/       — Vue Router with auth navigation guards
  types/        — TypeScript interfaces matching backend DTOs
```

### Key Frontend Files

| File | Role |
|---|---|
| `api/axios.ts` | Axios instances with JWT interceptor |
| `stores/authStore.ts` | JWT token, login/logout, localStorage persistence |
| `api/cartApi.ts` | Cart API client (CRUD operations against `/cart`) |
| `stores/cartStore.ts` | Cart state, localStorage + backend sync (local-first strategy) |
| `stores/orderStore.ts` | Orders list, place order, status updates |
| `composables/useOrderStatus.ts` | STOMP WebSocket subscription per order (auto-reconnect) |
| `router/index.ts` | Routes + `beforeEach` auth guard (saves redirect destination) |
| `types/index.ts` | TypeScript interfaces (must match backend DTOs exactly) |

### Key Frontend Behaviors

- **Cart persistence:** Local-first strategy — `cartStore` syncs to `localStorage` (watch + deep:true) and to backend API when logged in. `syncWithBackend()` merges local + server cart on login
- **Login redirect:** Router guard saves the intended destination as `?redirect=` query param; `LoginView`/`RegisterView` redirect back after auth
- **Order items:** `OrderItem` entity stores `productName` as a snapshot at order time (not looked up from product-service)
- **Live status in list:** `OrdersView` opens a single STOMP connection and subscribes to all CREATED orders for live status updates
- **GET /orders** returns Spring `Page<OrderResponse>` — frontend reads `response.data.content` (not `response.data` directly)

### CORS

All three backend services (user, product, order) have:
- `CorsConfig.java` — allows `http://localhost:5173`
- `.cors(Customizer.withDefaults())` in SecurityConfig — lets preflight OPTIONS pass before auth check