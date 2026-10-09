# Subscription Service API Contract

## Authentication

Send:

`Authorization: Bearer <JWT>`

Required role: `DEALER`.

## Endpoints

### POST /api/subscriptions
Creates an ACTIVE subscription.

Request:
```json
{
  "dealerId": 4,
  "cropId": 101,
  "category": "VEGETABLES"
}
```

Success: `201 Created`.

### GET /api/subscriptions/{id}
Returns a subscription owned by the authenticated dealer.

Success: `200 OK`.

### GET /api/subscriptions/dealer/{dealerId}
Returns all subscriptions for the authenticated dealer.

Success: `200 OK`.

### GET /api/subscriptions/dealer/{dealerId}/active
Returns active subscriptions for the authenticated dealer.

Success: `200 OK`.

### PUT /api/subscriptions/{id}
Updates crop ID and category for an active subscription.

Request:
```json
{
  "cropId": 102,
  "category": "FRUITS"
}
```

Success: `200 OK`.

### PATCH /api/subscriptions/{id}/status
Changes status to `ACTIVE` or `CANCELLED`.

Request:
```json
{
  "status": "CANCELLED"
}
```

Success: `200 OK`.

### DELETE /api/subscriptions/{id}
Cancels the authenticated dealer's subscription.

Success: `200 OK`.

## Common errors

- `400 Bad Request` — validation error, duplicate subscription, invalid status, or ownership mismatch.
- `401 Unauthorized` — missing/invalid/expired JWT.
- `403 Forbidden` — authenticated user does not have the `DEALER` role.
- `500 Internal Server Error` — unexpected server-side error.
