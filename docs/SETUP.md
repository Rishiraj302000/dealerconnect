# DealerConnect — Setup & Deployment Guide

## Prerequisites

- Java 21 (JDK)
- MySQL 8 running on `localhost:3306`
- Maven (the bundled Maven Wrapper `mvnw`/`mvnw.cmd` can be used instead)

No manual database creation is required — each service creates its schema on first start
(`createDatabaseIfNotExist=true` plus Hibernate `ddl-auto=update`).

## Database configuration

Every data-owning service is configured for MySQL user `root` / password `root`. If your
credentials differ, update `spring.datasource.username` / `spring.datasource.password` in:

- `auth-service/src/main/resources/application.properties`
- `dealer-service/src/main/resources/application.properties`
- `favorite-service/src/main/resources/application.properties`
- `audit-service/src/main/resources/application.properties`

Schemas created automatically: `dealerconnect_auth`, `dealerconnect_dealer`,
`dealerconnect_favorite`, `dealerconnect_audit`.

## Build

Build all services (run in each module directory, or loop over them):

```bash
cd eureka-server     && ./mvnw clean package && cd ..
cd api-gateway       && ./mvnw clean package && cd ..
cd auth-service      && ./mvnw clean package && cd ..
cd dealer-service    && ./mvnw clean package && cd ..
cd favorite-service  && ./mvnw clean package && cd ..
cd audit-service     && ./mvnw clean package && cd ..
```

On Windows use `mvnw.cmd` instead of `./mvnw`. The tests boot a Spring context, so MySQL
must be running during the build of the data-owning services.

## Run

Start the services in this order, each in its own terminal. Wait until Eureka is up before
starting the rest, and give each client a few seconds to register.

```bash
# 1. Service registry (must be first)
cd eureka-server    && ./mvnw spring-boot:run

# 2. The remaining services (order among these is flexible)
cd auth-service     && ./mvnw spring-boot:run
cd api-gateway      && ./mvnw spring-boot:run
cd dealer-service   && ./mvnw spring-boot:run
cd favorite-service && ./mvnw spring-boot:run
cd audit-service    && ./mvnw spring-boot:run
```

You can also run the packaged jars:

```bash
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
# ...and so on for each service
```

## Verify

- Eureka dashboard: <http://localhost:8761> — should list `API-GATEWAY`, `AUTH-SERVICE`,
  `DEALER-SERVICE`, `FAVORITE-SERVICE`, `AUDIT-SERVICE` as `UP`.
- All client traffic goes through the gateway at <http://localhost:8080>.

### Smoke test (curl)

```bash
# Register an admin
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@dc.com","password":"secret1","role":"ADMIN"}'

# Login and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@dc.com","password":"secret1"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# Create a dealer
curl -X POST http://localhost:8080/dealers \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Acme Motors","code":"DLR-001","city":"Mumbai"}'

# View audit history (admin only)
curl http://localhost:8080/audit -H "Authorization: Bearer $TOKEN"
```

## Ports reference

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Dealer Service | 8082 |
| Favorite Service | 8083 |
| Audit Service | 8084 |

## Troubleshooting

- **`Access denied for user 'root'`** — update the datasource username/password in the four
  services' `application.properties`.
- **Gateway returns 503 for a valid route** — the target service has not registered with
  Eureka yet; wait a few seconds and retry.
- **401 on a protected route** — missing or expired `Authorization: Bearer` token; log in
  again. Tokens expire after one hour.
- **403 on a route** — the authenticated user's role is not permitted (see the route table
  in `API-DOCUMENTATION.md`).
- **Connection refused to `localhost:8761`** — start the Eureka Server first.
