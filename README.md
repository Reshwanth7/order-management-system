# Order Management System

Microservices project built with Spring Boot 3.2.4 + Spring Cloud 2023.0.1

## Services
- **eureka-server** — Service registry (port 8761)
- **api-gateway** — Single entry point, routes all requests (port 9093)
- **user-service** — User management REST API (port 9091)
- **order-service** — Order management REST API (port 9094)
- **config-server** — Centralised configuration server (port 8888)
- **zipkin** — Distributed tracing UI (port 9411)

## Architecture
Client → API Gateway (9093) → Eureka lookup → User Service (9091) / Order Service (9094) → H2 DB

Each service owns its own database (database-per-service pattern).
All services fetch shared config from Config Server on startup.
All services run as Docker containers on a shared bridge network (`microservices-net`).

## Tech Stack
- Java 21 (LTS)
- Spring Boot 3.2.4
- Spring Cloud Gateway + Netflix Eureka
- Spring Cloud Config Server (centralised config)
- Spring Data JPA + Hibernate
- H2 DB
- MapStruct 1.5.5 (compile-time DTO mapping)
- Lombok 1.18.30
- Bean Validation (Jakarta)
- OpenFeign (inter-service HTTP communication)
- Resilience4j (circuit breaker, retry, timeout)
- Micrometer Tracing + Zipkin (distributed tracing)
- Logback structured logging (traceId/spanId in every log line)
- Docker + Docker Compose

## API Endpoints

### Auth (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user, returns JWT |
| POST | /api/auth/login | Login, returns JWT |

### Users (protected — requires Bearer token)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/users | Create user |
| GET | /api/users/{id} | Get user by ID |

### Orders (protected — requires Bearer token)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/orders | Create a new order |
| GET | /api/orders/{id} | Get order by ID |

### Create user — example request
```json
POST http://localhost:9093/api/users
Content-Type: application/json

{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "securepass123"
}
```

### Create user — example response
```json
HTTP/1.1 201 Created
Location: /api/users/1

{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "createdAt": "2026-04-07T10:30:00"
}
```

## Security
All `/api/users/**` and `/api/orders/**` routes require:
```
Authorization: Bearer <jwt_token>
```
JWT secret is shared and aligned across services via Config Server. order-service runs its own `JwtAuthFilter` and `SecurityConfig` (no `UserDetailsService` required — token validation only).

## Centralised Configuration
Config Server serves shared configuration to all services on startup.

- JWT secret and expiration are defined once in Config Server — not duplicated across services
- Eureka URL is defined once in Config Server
- Services use `bootstrap.yml` + `spring-cloud-starter-bootstrap` to fetch config before the application context starts
- Config files stored at `config-server/src/main/resources/config/`
  - `user-service.yml`
  - `order-service.yml`

Inside Docker, config URIs use container names not localhost:
- Config Server: `http://config-server:8888`
- Eureka: `http://eureka-server:8761/eureka/`
- Zipkin: `http://zipkin:9411/api/v2/spans`

## Docker
Every service has a multi-stage `Dockerfile`:
- **Stage 1** — `eclipse-temurin:21-jdk-alpine` compiles and packages the JAR
- **Stage 2** — `eclipse-temurin:21-jre-alpine` runs only the JAR — no build tools, smaller image

All services are wired together via `docker-compose.yml` at the project root:
- Shared `microservices-net` bridge network — services find each other by container name
- Healthchecks on Config Server and Eureka — dependent services wait until healthy before starting
- Zipkin runs from the official `openzipkin/zipkin` image — no Dockerfile needed
- Startup order enforced: Zipkin → Config Server → Eureka → Services → Gateway

## Inter-Service Communication
order-service calls user-service via **OpenFeign** (`lb://user-service` — resolved through Eureka).
JWT tokens are forwarded automatically via a Feign `RequestInterceptor`. Trace context is propagated automatically via `feign-micrometer`, ensuring the same `traceId` flows across both services.

## Resilience (order-service → user-service)
order-service wraps all user-service calls with **Resilience4j**:

- **Retry** — retries transient failures before escalating
- **Circuit Breaker** — opens after repeated failures, preventing cascade; moves through Closed → Open → Half-Open states
- **TimeLimiter** — enforces a timeout on calls
- **Fallback** — returns an order with `QUEUED` status instantly when user-service is unavailable

Execution order: `Retry → CircuitBreaker → TimeLimiter → Fallback`

AOP is required for Resilience4j annotations to function correctly.

## Distributed Tracing
Every request gets a `traceId` that flows across all services and is visible in both logs and Zipkin UI.

- **Micrometer Tracing** + **Brave** — generates and propagates traceId/spanId
- **Zipkin** — collects and visualises traces at `http://localhost:9411`
- **feign-micrometer** — ensures Feign calls carry the same traceId to user-service
- **B3 propagation** — trace headers passed automatically across service boundaries
- Zipkin endpoint is read from `application.yml` via `@Value` — no hardcoded URLs

Every log line includes `traceId/spanId`:
```
06:44:21 [69d8d4846c3a5f57b4d87774abb26124/dbd907029299a3b2] INFO ...
```

## Database
- H2 in-memory database (development)
- Console available at `http://localhost:9091/h2-console` when running locally
- JDBC URL: `jdbc:h2:mem:userdb` — Username: `sa` — Password: *(blank)*
- Hibernate auto-manages schema via `ddl-auto: create-drop`
- Will migrate to PostgreSQL 16 in Day 12

## How to run (Docker — recommended)
```bash
# From the root of order-management-system/
docker compose up --build
```

Startup order is handled automatically by healthchecks and `depends_on` in `docker-compose.yml`.

Test via gateway:
```bash
# Register and get a token
curl -X POST http://localhost:9093/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"securepass123"}'

# Login
curl -X POST http://localhost:9093/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"securepass123"}'

# Create an order (replace <token> with JWT from above)
curl -X POST http://localhost:9093/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId":1,"productName":"Laptop","quantity":1,"totalPrice":999.99}'

# Get an order
curl http://localhost:9093/api/orders/1 \
  -H "Authorization: Bearer <token>"

# Invalid userId — expect 404
curl -X POST http://localhost:9093/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId":999,"productName":"Laptop","quantity":1,"totalPrice":999.99}'

# No token — expect 401
curl -X POST http://localhost:9093/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"Laptop","quantity":1,"totalPrice":999.99}'
```

Useful URLs once running:
- Eureka dashboard: `http://localhost:8761`
- Config Server: `http://localhost:8888/user-service/default`
- Zipkin UI: `http://localhost:9411`

## Project Structure
Multi-module Maven project with a parent `pom.xml` — all services are registered as modules for proper IntelliJ recognition.

## Project Progress
| Day | Focus | Status |
|-----|-------|--------|
| Day 1 | Project setup, Eureka, Gateway routing | ✅ Done |
| Day 2 | H2, JPA, Hibernate, Lombok | ✅ Done |
| Day 3 | DTOs, MapStruct, Bean Validation | ✅ Done |
| Day 4 | Global exception handling | ✅ Done |
| Day 5 | Spring Security + JWT auth | ✅ Done |
| Day 6 | order-service + OpenFeign inter-service comms | ✅ Done |
| Day 7 | Resilience4j circuit breaker + retry + timeout | ✅ Done |
| Day 8 | Distributed tracing + structured logging | ✅ Done |
| Day 9 | Spring Cloud Config Server | ✅ Done |
| Day 10 | Consolidation + end-to-end verification | ✅ Done |
| Day 11 | Docker — Dockerfile + Docker Compose + Zipkin | ✅ Done |
| Day 12 | PostgreSQL + multi-stage build polish | 🔜 Next |