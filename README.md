# Order Management System

Microservices project built with Spring Boot 3.2.4 + Spring Cloud 2023.0.1

## Services
- **eureka-server** — Service registry (port 8761)
- **api-gateway** — Single entry point, routes all requests (port 9093)
- **user-service** — User management REST API (port 9091)

## Architecture
Client → API Gateway (9093) → Eureka lookup → User Service (9091) → PostgreSQL

## Tech Stack
- Java 21 (LTS)
- Spring Boot 3.2.4
- Spring Cloud Gateway + Netflix Eureka
- Spring Data JPA + Hibernate
- PostgreSQL 16
- MapStruct 1.5.5 (compile-time DTO mapping)
- Lombok 1.18.30
- Bean Validation (Jakarta)

## API Endpoints

### User Service (via gateway on port 9093)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/users | Create a new user |
| GET | /api/users/{id} | Get user by ID |

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

## Database
- PostgreSQL running locally on port 5432
- Database name: `userdb`
- Hibernate auto-manages schema via `ddl-auto: update`

## How to run
1. Start PostgreSQL and ensure `userdb` database exists
2. Start `eureka-server`
3. Start `user-service`
4. Start `api-gateway`
5. Test via gateway:
```bash
# Create a user
curl -X POST http://localhost:9093/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"securepass123"}'

# Get a user
curl http://localhost:9093/api/users/1
```

## Project Progress
| Day | Focus | Status |
|-----|-------|--------|
| Day 1 | Project setup, Eureka, Gateway routing | ✅ Done |
| Day 2 | PostgreSQL, JPA, Hibernate, Lombok | ✅ Done |
| Day 3 | DTOs, MapStruct, Bean Validation | ✅ Done |
| Day 4 | Global exception handling | 🔜 Next |