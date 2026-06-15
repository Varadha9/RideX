# trip-service

Manages the lifecycle of an active trip. Consumes Kafka events from booking-service to create trip records, and publishes `ride-completed` when a trip ends.

- **Port:** 8084
- **Database:** MySQL (`ridex`) + Kafka consumer + producer
- **Swagger:** http://localhost:8084/swagger-ui.html

## Endpoints

| Method | Endpoint                          | Description             |
|--------|-----------------------------------|-------------------------|
| PUT    | /api/trips/start/{bookingId}      | Start a trip            |
| PUT    | /api/trips/end/{bookingId}        | End a trip              |
| GET    | /api/trips/history/user/{userId}  | User trip history       |
| GET    | /api/trips/history/driver/{id}    | Driver trip history     |

## Kafka Events Consumed

| Topic           | Action                              |
|-----------------|-------------------------------------|
| ride-requested  | Creates a new Trip record in MySQL  |
| driver-accepted | Links driverId to the Trip          |

## Kafka Events Published

| Action    | Topic          | Consumed By                          |
|-----------|----------------|--------------------------------------|
| Trip ended| ride-completed | analytics-service, notification-service |

## Trip Status Flow

```
PENDING → IN_PROGRESS → COMPLETED
```

## Start Trip Example

```bash
curl -X PUT http://localhost:8084/api/trips/start/1
```

## End Trip Example

```bash
curl -X PUT http://localhost:8084/api/trips/end/1
```
