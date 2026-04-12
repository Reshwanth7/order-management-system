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

## Apr 10 2026 — Phase 1 Day 8 COMPLETE
**Built:** Distributed tracing with Micrometer + Zipkin and structured logging across user-service and order-service
**Learned:** How traceId/spanId propagate across microservices, why actuator is required for tracing auto-configuration, Zipkin reporter async flush behaviour, feign-micrometer for automatic trace context propagation in Feign calls, B3 propagation headers (X-B3-TraceId, X-B3-SpanId), why spring.zipkin.base-url is a Spring Cloud Sleuth property and doesn't exist in Spring Boot 3.x (use management.zipkin.tracing.endpoint instead)
**Bugs fixed:** Traces not appearing in Zipkin (missing spring-boot-starter-actuator in user-service), traceId showing [/] instead of actual IDs (zipkin-sender-urlconnection missing), different traceIds across services (added feign-micrometer dependency to wire Feign into Micrometer tracing context), Java version mismatch running Zipkin JAR (used full Java 21 path instead of system default Java 8)
**Tests passing:** Happy path POST /api/orders → single traceId visible in Zipkin spanning both services, traceId/spanId visible in every log line, cross-service traceId matches between order-service logs and Zipkin, user-service down → fallback fires → Zipkin shows order-service span only with circuit open
**Next:** Day 9 — Config Server

## Apr 12 2026 — Phase 1 Day 9 COMPLETE
**Built:** Spring Cloud Config Server — centralised configuration for user-service and order-service
**Learned:** Why centralised config matters (no more duplicated secrets across services), how Config Server serves yml files per service name, native profile vs Git-backed config, why bootstrap.yml loads before application.yml (bootstrap phase), why spring-cloud-starter-bootstrap is required in Spring Boot 3.x for bootstrap.yml to be picked up, how @Value still works unchanged when values come from Config Server
**Bugs fixed:** jwt.secret placeholder not resolving (services starting before config fetched — fixed with bootstrap.yml + spring-cloud-starter-bootstrap), ${project.build.directory} in classpath instead of target (Maven not compiled properly — fixed with clean compile + Maven reload), root pom.xml not recognised by IntelliJ Maven panel (added via Maven panel + button then right-click → Add as Maven Project)
**Tests passing:** http://localhost:8888/user-service/default returns JWT secret and Eureka URL, http://localhost:8888/order-service/default returns config, user-service and order-service start and fetch config from Config Server, full end-to-end POST /api/orders with JWT works with no local secret config
**Next:** Day 10 — Consolidation

## Apr 12 2026 — Phase 1 Day 10 COMPLETE
**Built:** n/a — consolidation and end-to-end verification day
**Learned:** Importance of startup order in a microservices system (Config Server must start before services that depend on it), how all the pieces built in Days 1–9 work together as a system, how to verify the full stack is healthy before moving to Docker week
**Bugs fixed:** n/a — clean consolidation
**Tests passing:** Full startup order verified (Zipkin → Config Server → Eureka → user-service → order-service → api-gateway), POST /api/auth/register returns token, POST /api/auth/login returns token, POST /api/orders with valid token + valid userId returns 201, GET /api/orders/{id} returns order, POST /api/orders with invalid userId returns 404, POST /api/orders without token returns 401
**Next:** Day 11 — Docker (Dockerfile + multi-stage builds)