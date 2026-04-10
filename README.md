# Order Management System

Microservices project built with Spring Boot 3.2.4 + Spring Cloud 2023.0.1

## Services
- **eureka-server** — Service registry (port 8761)
- **api-gateway** — Single entry point, routes all requests (port 9093)
- **user-service** — User management REST API (port 9091)
- **order-service** — Order management REST API (port 9094)

## Architecture
Client → API Gateway (9093) → Eureka lookup → User Service (9091) / Order Service (9094) → H2 DB

Each service owns its own database (database-per-service pattern).

## Tech Stack
- Java 21 (LTS)
- Spring Boot 3.2.4
- Spring Cloud Gateway + Netflix Eureka
- Spring Data JPA + Hibernate
- H2 DB
- MapStruct 1.5.5 (compile-time DTO mapping)
- Lombok 1.18.30
- Bean Validation (Jakarta)
- OpenFeign (inter-service HTTP communication)
- Resilience4j (circuit breaker, retry, timeout)
- Micrometer Tracing + Zipkin (distributed tracing)
- Logback structured logging (traceId/spanId in every log line)

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
JWT secret is shared and aligned across services. order-service runs its own `JwtAuthFilter` and `SecurityConfig` (no `UserDetailsService` required — token validation only).

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

Run Zipkin locally without Docker:
```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
# Use full Java 21 path if system default is older
"C:\Users\<user>\.jdks\<jdk-21>\bin\java" -jar zipkin.jar
```

Every log line includes `traceId/spanId`:
```
06:44:21 [69d8d4846c3a5f57b4d87774abb26124/dbd907029299a3b2] INFO ...
```

## Database
- H2 in-memory database (development)
- Console available at `http://localhost:9091/h2-console` when app is running
- JDBC URL: `jdbc:h2:mem:userdb` — Username: `sa` — Password: *(blank)*
- Hibernate auto-manages schema via `ddl-auto: create-drop`
- Will migrate to PostgreSQL 16 once local setup is resolved

## How to run
1. Start Zipkin: `"<java21-path>/bin/java" -jar zipkin.jar`
2. Start `eureka-server`
3. Start `user-service`
4. Start `order-service`
5. Start `api-gateway`
6. Test via gateway:
```bash
# Register and get a token
curl -X POST http://localhost:9093/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"securepass123"}'

# Create an order (replace <token> with JWT from above)
curl -X POST http://localhost:9093/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId":1,"item":"Laptop","quantity":1}'

# Get an order
curl http://localhost:9093/api/orders/1 \
  -H "Authorization: Bearer <token>"
```

7. View traces at `http://localhost:9411`

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
| Day 9 | Docker | 🔜 Next |