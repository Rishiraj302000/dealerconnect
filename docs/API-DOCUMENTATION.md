# DealerConnect — API Documentation

All client requests go through the API Gateway at `http://localhost:8080`. Except for the
two public auth endpoints, every request must include a JWT:

```
Authorization: Bearer <token>
```

Roles: `ADMIN`, `RELATIONSHIP_MANAGER`.

Common error response shape:

```json
{
  "timestamp": "2026-06-15T13:02:24.34",
  "status": 409,
  "error": "Conflict",
  "message": "Dealer code already exists: DLR-001",
  "fieldErrors": null
}
```

Validation failures additionally populate `fieldErrors` (field name → message).

---

## Auth Service — `/auth`

### POST /auth/register
Public. Creates a user.

Request:
```json
{ "name": "Alice Admin", "email": "alice@dc.com", "password": "secret1", "role": "ADMIN" }
```
Response `201 Created`:
```json
{ "id": 1, "name": "Alice Admin", "email": "alice@dc.com", "role": "ADMIN", "createdAt": "2026-06-15T13:02:24.43" }
```
Errors: `400` validation, `409` email already registered.

### POST /auth/login
Public. Authenticates and returns a JWT.

Request:
```json
{ "email": "alice@dc.com", "password": "secret1" }
```
Response `200 OK`:
```json
{ "token": "eyJhbGciOiJIUzUxMiJ9...", "type": "Bearer", "email": "alice@dc.com", "role": "ADMIN" }
```
Errors: `400` validation, `401` invalid credentials.

---

## Dealer Service — `/dealers`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/dealers` | ADMIN | Create dealer |
| GET | `/dealers` | ADMIN, RM | List all (non-deleted) |
| GET | `/dealers/search?name=&city=&status=` | ADMIN, RM | Search (all params optional) |
| GET | `/dealers/{id}` | ADMIN, RM | Get one |
| PUT | `/dealers/{id}` | ADMIN | Update |
| PATCH | `/dealers/{id}/activate` | ADMIN | Set status ACTIVE |
| PATCH | `/dealers/{id}/deactivate` | ADMIN | Set status INACTIVE |
| DELETE | `/dealers/{id}` | ADMIN | Soft delete |

Create/Update request:
```json
{ "name": "Acme Motors", "code": "DLR-001", "email": "info@acme.com", "phone": "9999999999", "city": "Mumbai" }
```
Dealer response:
```json
{
  "id": 1, "name": "Acme Motors", "code": "DLR-001", "email": "info@acme.com",
  "phone": "9999999999", "city": "Mumbai", "status": "ACTIVE",
  "createdAt": "2026-06-15T13:02:24.43", "updatedAt": "2026-06-15T13:02:24.43"
}
```
`status` query value is `ACTIVE` or `INACTIVE`. Errors: `400` validation, `404` not found,
`409` duplicate code. `DELETE` returns `204 No Content`.

Events emitted to the Audit Service: `DEALER_CREATED`, `DEALER_UPDATED`,
`DEALER_ACTIVATED`, `DEALER_DEACTIVATED`.

---

## Dealer Contact Service — `/contacts`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/contacts` | ADMIN | Create contact |
| GET | `/contacts?dealerId={id}` | ADMIN, RM | List contacts of a dealer |
| GET | `/contacts/{id}` | ADMIN, RM | Get one |
| PUT | `/contacts/{id}` | ADMIN | Update |
| DELETE | `/contacts/{id}` | ADMIN | Delete |

Create/Update request:
```json
{ "dealerId": 1, "name": "John Smith", "email": "john@acme.com", "phone": "8888888888", "designation": "Sales Manager" }
```
Contact response:
```json
{ "id": 1, "dealerId": 1, "name": "John Smith", "email": "john@acme.com", "phone": "8888888888", "designation": "Sales Manager", "createdAt": "2026-06-15T13:02:24.83" }
```
Errors: `400` validation, `404` dealer or contact not found. `DELETE` returns `204`.

---

## Favorite Service — `/favorites`

The owner is taken from the `X-Auth-User` header the gateway injects, so favorites are
always scoped to the calling user.

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/favorites` | ADMIN, RM | Add favorite (verifies dealer exists via Feign) |
| GET | `/favorites?category={c}` | ADMIN, RM | View favorites; `category` optional filter |
| DELETE | `/favorites/{id}` | ADMIN, RM | Remove favorite |

Add request:
```json
{ "dealerId": 1, "category": "Premium" }
```
Favorite response:
```json
{ "id": 1, "username": "alice@dc.com", "dealerId": 1, "dealerName": "Acme Motors", "dealerCode": "DLR-001", "category": "Premium", "createdAt": "2026-06-15T13:29:41.29" }
```
Errors: `400` validation, `404` dealer not found, `409` already favorited,
`503` Dealer Service unavailable. `DELETE` returns `204`.

Events emitted to the Audit Service: `FAVORITE_ADDED`, `FAVORITE_REMOVED`.

---

## Audit Service — `/audit`

| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/audit?action=&entityType=&performedBy=` | ADMIN | Audit history, newest first; all filters optional |
| POST | `/audit` | internal | Create record (called by services via Feign, not for external use) |

Audit response item:
```json
{ "id": 1, "action": "DEALER_CREATED", "entityType": "DEALER", "entityId": 9, "performedBy": "alice@dc.com", "details": "Dealer 'Acme Motors' (code DLR-001) created", "timestamp": "2026-06-15T13:02:24.66" }
```
`entityType` is `DEALER` or `FAVORITE`. Non-admin access returns `403` at the gateway.

---

## Status code summary

| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 204 | No Content (delete) |
| 400 | Validation error |
| 401 | Missing/invalid/expired token |
| 403 | Authenticated but role not permitted |
| 404 | Resource not found |
| 409 | Duplicate (email, dealer code, favorite) |
| 503 | Downstream service unavailable |
