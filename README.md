# DealerConnect

A microservice-based Dealer Relationship Management and Auditing platform built with
Java 21, Spring Boot 4.1, and Spring Cloud 2025.1. It provides secure, role-based dealer
management with centralized JWT authentication, service discovery, inter-service
communication, soft-delete governance, dealer favorites, and a complete audit trail.

## Microservices

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service registry |
| API Gateway | 8080 | Single entry point: routing, JWT validation, role authorization |
| Auth Service | 8081 | Registration, login, BCrypt, JWT issuing |
| Dealer Service | 8082 | Dealer & dealer-contact CRUD, search, activate/deactivate, soft delete |
| Favorite Service | 8083 | Dealer favorites (verifies dealers via OpenFeign) |
| Audit Service | 8084 | Stores and serves the audit history |

## Architecture at a glance

- **Centralized security**: only the gateway validates JWTs. It checks role-based route
  rules and forwards the identity downstream as `X-Auth-User` / `X-Auth-Role` headers, so
  the protected services trust the gateway rather than re-validating tokens.
- **Service discovery**: every service registers with Eureka; the gateway and Feign clients
  resolve targets by service name (`lb://SERVICE-NAME`).
- **Inter-service calls (OpenFeign)**: Favorite Service verifies a dealer exists before
  favoriting it; Dealer and Favorite services publish business events to the Audit Service.
- **Database per service**: each data-owning service has its own MySQL schema.

## Roles

- `ADMIN` — full dealer/contact management, activate/deactivate, soft delete, audit history.
- `RELATIONSHIP_MANAGER` — search dealers, read dealer/contact data, manage own favorites.

## Documentation

- [Architecture](docs/ARCHITECTURE.md) — components, security model, request flows.
- [ER Diagram](docs/ER-DIAGRAM.md) — databases, tables, relationships.
- [API Documentation](docs/API-DOCUMENTATION.md) — endpoints, payloads, status codes.
- [Setup & Deployment](docs/SETUP.md) — prerequisites, build, run, verification.

## Quick start

1. Start MySQL on `localhost:3306` (user `root` / password `root`, or adjust the four
   services' `application.properties`).
2. Build everything (see [Setup](docs/SETUP.md)).
3. Start the Eureka Server first, then the other services.
4. Send all requests through the gateway at `http://localhost:8080`.

## Technology

Java 21 · Spring Boot 4.1 · Spring Cloud 2025.1 · Spring Cloud Gateway · Netflix Eureka ·
OpenFeign · Spring Security · JWT (jjwt) · Spring Data JPA · MySQL · Lombok · Maven
