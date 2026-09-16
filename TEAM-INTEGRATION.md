# CropDeal Team Integration Notes

## Service registry names

- AUTH-SERVICE
- USER-SERVICE
- CROP-SERVICE
- ORDER-SERVICE
- PAYMENT-SERVICE
- NOTIFICATION-SERVICE
- REPORT-REVIEW-INVOICE-SERVICE

All services should register with:

`http://localhost:8761/eureka/`

## API Gateway

Gateway port: `8080`.

Current Member 1 route:

`/api/auth/**` → `lb://AUTH-SERVICE`

When the other members add their services, add routes for their API prefixes in `api-gateway/GatewayRoutesConfig.java`.

## Config Server

Port: `8888`.

Member 1 currently keeps the shared config templates in:

`config-server/src/main/resources/config/`

Add team-service property files there when integrating the other services.

## RabbitMQ use case

For this case study, the clean messaging use case is notification events:

- crop published → notify subscribed dealers
- crop updated → notify subscribed dealers
- order placed → notify farmer/dealer
- payment completed → notify both parties

Notification-service should consume the events. Do not make the auth service depend on RabbitMQ unless the team specifically decides to publish an authentication event.

## Authentication contract

Successful login returns:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

Downstream services should receive:

`Authorization: Bearer <accessToken>`

JWT claims:

- `sub` = user email
- `role` = ADMIN / FARMER / DEALER

## Role names

```text
ADMIN
FARMER
DEALER
```

## Important ownership rule

Auth-service owns authentication credentials, JWT, refresh tokens, password reset tokens and activation state.

User-service should own farmer/dealer profile information such as bank details and profile data rather than duplicating credentials.
