# Learning Log

## Apr 6 2026 — Phase 1 Day 1 COMPLETE
**Built:** eureka-server + api-gateway + user-service
**Learned:** Monolith vs microservices, Gateway routing, Eureka discovery
**Bugs fixed:** Version mismatch, missing loadbalancer dep, @PathVariable Java 21
**Tests passing:** All 4 curl tests through gateway
**Next:** Day 2 — PostgreSQL + JPA in user-service

## Apr 7 2026 — Phase 1 Day 2 COMPLETE
**Built:** H2 DB integration + full JPA persistence layer in user-service
**Learned:** JPA/Hibernate stack (Entity → Repository → HikariCP → PostgreSQL), @Transactional behaviour, ddl-auto strategies, HikariCP connection pooling
**Bugs fixed:** Docker Desktop virtualization error (switched to native PostgreSQL install), Java 25 SDK conflict (downgraded to Java 21 LTS)
**Tests passing:** POST /api/users and GET /api/users/{id} through gateway with PostgreSQL persistence
**Next:** Day 3 — DTOs + MapStruct + Validation

## Apr 7 2026 — Phase 1 Day 3 COMPLETE
**Built:** DTO layer with Java records, MapStruct mapper, Bean Validation on incoming requests
**Learned:** Why entities should never leave the service layer, MapStruct compile-time generation vs reflection-based mappers, @Valid + constraint annotations, ResponseEntity with proper 201 Created + Location header
**Bugs fixed:** ExceptionInInitializerError caused by Java 25 SDK incompatibility with Lombok, MapStruct annotation processor order (Lombok → binding → MapStruct), lombok-mapstruct-binding requirement
**Tests passing:** POST with valid body returns 201 + UserResponse (no passwordHash), POST with invalid body returns 400 with constraint violations, GET /api/users/{id} returns clean DTO
**Next:** Day 4 — Global exception handling with @ControllerAdvice

## Apr 8 2026 — Phase 1 Day 4 COMPLETE
**Built:** Global exception handling across all controllers in user-service
**Learned:** @ControllerAdvice as a global safety net, @ExceptionHandler mapping exceptions to HTTP responses, custom business exceptions extending RuntimeException, 401 vs 403 distinction and AuthenticationEntryPoint
**Bugs fixed:** Raw RuntimeException leaking stack traces replaced with clean ErrorResponse JSON
**Tests passing:** 404 user not found, 409 duplicate email, 400 validation failure, 500 fallback — all return consistent JSON error shape
**Next:** Day 5 — Spring Security + JWT

## Apr 8 2026 — Phase 1 Day 5 COMPLETE
**Built:** Full JWT authentication — register, login, protected routes, 401 on missing token
**Learned:** JWT structure (header.payload.signature), stateless auth vs session-based, Spring Security filter chain, OncePerRequestFilter, SecurityContextHolder, BCrypt password hashing, AuthenticationEntryPoint for proper 401 responses
**Bugs fixed:** Gateway 404 for /api/auth/** (added second route in gateway yml), 403 instead of 401 on missing token (added JwtAuthEntryPoint wired into SecurityConfig)
**Tests passing:** POST /api/auth/register returns token, POST /api/auth/login returns token, GET /api/users/{id} with token returns 200, GET /api/users/{id} without token returns 401
**Next:** Day 6 — order-service + inter-service communication

## Apr 9 2026 — Phase 1 Day 6 COMPLETE
**Built:** order-service from scratch with full inter-service communication to user-service via OpenFeign
**Learned:** Why each microservice owns its own database, OpenFeign declarative HTTP client, JWT forwarding via RequestInterceptor, Feign resolves service address via Eureka (lb://), SecurityConfig in order-service without UserDetailsService, parent pom.xml for multi-module IntelliJ project
**Bugs fixed:** 500 on invalid userId (added UserNotFoundException + GlobalExceptionHandler to order-service), order-service open without token (added Spring Security + JwtAuthFilter), port conflict on 9092 (moved order-service to 9094), JWT secret mismatch between services (aligned secrets), IntelliJ not recognising order-service (added parent pom.xml with modules)
**Tests passing:** POST /api/orders with valid token + valid userId returns 201, POST /api/orders with invalid userId returns 404, POST /api/orders without token returns 401, GET /api/orders/{id} returns order
**Next:** Day 7 — Resilience4j circuit breaker + retry

## Apr 9 2026 — Phase 1 Day 7 COMPLETE
**Built:** Resilience4j circuit breaker + retry + timeout on order-service → user-service calls
**Learned:** Circuit breaker three states (Closed/Open/Half-Open), why retry and circuit breaker serve different purposes, execution order (Retry → CircuitBreaker → TimeLimiter → Fallback), fallback method signature must match original method + Exception param, AOP required for Resilience4j annotations
**Bugs fixed:** n/a — clean implementation
**Tests passing:** Happy path returns 201, user-service stopped → fallback returns QUEUED status instantly with no 500, circuit reopens automatically after 10s wait when user-service restarts
**Next:** Day 8 — Distributed tracing + structured logging