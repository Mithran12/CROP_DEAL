# CropDeal Subscription Service

Dealer subscription microservice for the CropDeal application.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- MySQL
- Spring Security + JWT validation
- RabbitMQ
- Eureka Discovery Client
- Spring Cloud Config Client
- SpringDoc OpenAPI / Swagger UI
- Spring Boot Actuator
- JUnit 5 + Mockito
- Docker / Docker Compose

## Responsibilities

1. Create dealer crop subscriptions.
2. Prevent duplicate active subscriptions for the same dealer and crop.
3. View one subscription.
4. View all subscriptions for a dealer.
5. View active subscriptions.
6. Update an active subscription.
7. Change subscription status.
8. Cancel a subscription.
9. Consume `crop.published` events from RabbitMQ.
10. Publish `subscription.notification` events for matching active subscriptions.
11. Validate JWTs and allow subscription APIs only to users with the `DEALER` role.

## Port and database

- Service port: `8088`
- Database: `cropdeal_subscription_db`
- Local MySQL: `localhost:3306`
- Docker MySQL: `localhost:3307`

Create the local database if required:

```sql
CREATE DATABASE cropdeal_subscription_db;
```

The configured local MySQL credentials are:

```text
username: root
password: system
```

## REST API

All `/api/subscriptions/**` endpoints require:

```text
Authorization: Bearer <JWT>
```

The JWT must contain the `DEALER` role. The dealer identifier should be available as `userId`, `id`, `dealerId`, or a numeric `sub` claim.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/subscriptions` | Create subscription |
| GET | `/api/subscriptions/{id}` | Get one owned subscription |
| GET | `/api/subscriptions/dealer/{dealerId}` | Get dealer subscriptions |
| GET | `/api/subscriptions/dealer/{dealerId}/active` | Get active dealer subscriptions |
| PUT | `/api/subscriptions/{id}` | Update crop/category |
| PATCH | `/api/subscriptions/{id}/status` | Set ACTIVE/CANCELLED |
| DELETE | `/api/subscriptions/{id}` | Cancel subscription |

### Create request

```json
{
  "dealerId": 4,
  "cropId": 101,
  "category": "VEGETABLES"
}
```

### Update request

```json
{
  "cropId": 102,
  "category": "FRUITS"
}
```

### Status request

```json
{
  "status": "CANCELLED"
}
```

### Response

```json
{
  "id": 1,
  "dealerId": 4,
  "cropId": 101,
  "category": "VEGETABLES",
  "status": "ACTIVE",
  "createdAt": "2026-09-16T12:20:00",
  "updatedAt": "2026-09-16T12:20:00"
}
```

## Security

Subscription Service does not implement login or issue tokens.

The Auth Service remains responsible for authentication and JWT issuance. Subscription Service only validates the JWT signature and expiration and checks the `DEALER` role.

Set the same signing secret used by Auth Service through the environment variable:

```text
JWT_SECRET=<team-shared-secret>
```

Do not commit a real production secret to Git.

For standalone development, `application.properties` contains a local fallback secret only.

## RabbitMQ

Current messaging contract:

```text
Exchange: cropdeal.events
Incoming queue: subscription.crop.published.queue
Incoming routing key: crop.published
Outgoing routing key: subscription.notification
```

Flow:

```text
Crop Service
    |
    | crop.published
    v
Subscription Service
    |
    | finds matching ACTIVE subscriptions
    v
subscription.notification
    |
    v
Notification Service
```

The event listener accepts JSON containing `cropId` and/or `category`.

The final team integration must use the exact exchange, queue, routing-key, and JSON field names agreed by the Crop and Notification services.

RabbitMQ does not have to be running for the Spring application to start during standalone development. The listener will connect when RabbitMQ becomes available.

## Swagger and Actuator

Swagger UI:

```text
http://localhost:8088/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8088/v3/api-docs
```

Health:

```text
http://localhost:8088/actuator/health
```

## Standalone run

1. Start MySQL.
2. Create `cropdeal_subscription_db`.
3. Ensure the MySQL credentials in `application.properties` are correct.
4. RabbitMQ is optional until messaging testing.
5. Run the Spring Boot application.

Eureka and Config Server are disabled in standalone mode:

```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
spring.cloud.config.enabled=false
```

During team integration, move shared configuration to the team's Config Server and enable Eureka registration.

## Docker

Build the application first so the jar exists in `target/`, then:

```bash
docker compose up --build
```

Docker Compose starts:

- MySQL on host port `3307`
- RabbitMQ on `5672`
- RabbitMQ Management UI on `15672`
- Subscription Service on `8088`

## Tests

Unit tests cover:

- successful subscription creation
- duplicate active subscription rejection
- missing subscription handling
- status update
- dealer ownership protection

Run:

```bash
mvn test
```

## Project structure

```text
src/main/java/com/cropdeal
├── SubscriptionServiceApplication.java
└── subscription
    ├── config
    ├── controller
    ├── dto
    ├── entity
    ├── exception
    ├── messaging
    ├── repository
    ├── security
    └── service
```

## Git

Do not commit:

```text
target/
*.log
.env
.metadata/
.settings/
.classpath
.project
```
