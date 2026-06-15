# user-service

Handles rider registration, login, and JWT-based authentication.

- **Port:** 8081
- **Database:** MySQL (`ridex`)
- **Swagger:** http://localhost:8081/swagger-ui.html

## Endpoints

| Method | Endpoint               | Auth   | Description              |
|--------|------------------------|--------|--------------------------|
| POST   | /api/users/register    | Public | Register a new rider     |
| POST   | /api/users/login       | Public | Login, returns JWT token |
| GET    | /api/users/profile     | JWT    | Get current user profile |
| PUT    | /api/users/update/{id} | JWT    | Update profile           |

## JWT Authentication

Login returns a token:
```json
{ "token": "eyJhbGci..." }
```

Pass it in all authenticated requests:
```
Authorization: Bearer <token>
```

## Register Example

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@ridex.com","password":"pass123","phone":"9999999999"}'
```

## Login Example

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@ridex.com","password":"pass123"}'
```

## Security Config

Public paths: `/api/users/register`, `/api/users/login`, `/swagger-ui/**`, `/api-docs/**`

All other routes require a valid JWT.

## Roles

`USER`, `DRIVER`, `ADMIN`
