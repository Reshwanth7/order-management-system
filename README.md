# Order Management System

Microservices project built with Spring Boot 3.2.4 + Spring Cloud 2023.0.1

## Services
- **eureka-server** — Service registry (port 8761)
- **api-gateway** — Single entry point, routes all requests (port 9093)  
- **user-service** — User management REST API (port 9091)

## Architecture
Client → API Gateway (9093) → Eureka lookup → User Service (9091)

## Tech Stack
Java 21, Spring Boot 3.2.4, Spring Cloud Gateway, Netflix Eureka

## How to run
1. Start eureka-server
2. Start user-service
3. Start api-gateway
4. Test: curl http://localhost:9093/api/users/hello
