# RideX — Cab Booking Platform

A production-grade microservices backend inspired by Uber/Ola, demonstrating real-world backend engineering and system design.

## Architecture

```
Client (Mobile/Web)
        │
    API Gateway (future: Spring Cloud Gateway)
        │
┌───────────────────────────────────────────┐
│  User     Driver   Booking  Trip  Payment │
│  :8081    :8082    :8083   :8084  :8085   │
│  Pricing  Notification  Analytics         │
│  :8086    :8087          :8088            │
└───────────────────────────────────────────┘
        │               │
    [ Kafka ]      [ RabbitMQ ]
        │
    [ Redis GEO ]
        │
  [ MySQL ] [ PostgreSQL ]
```

## Services

| Service              | Port | DB         | Key Feature                        |
|----------------------|------|------------|------------------------------------|
| user-service         | 8081 | MySQL      | JWT auth, rider accounts           |
| driver-service       | 8082 | MySQL+Redis| Redis GEO nearest driver search    |
| booking-service      | 8083 | MySQL+Kafka| Core booking + Kafka event publish |
| trip-service         | 8084 | MySQL+Kafka| Trip lifecycle, Kafka consumer     |
| payment-service      | 8085 | MySQL+Kafka| WALLET/UPI/CARD simulation         |
| pricing-service      | 8086 | —          | Dynamic fare: peak + night rates   |
| notification-service | 8087 | Kafka+AMQP | Kafka→RabbitMQ bridge              |
| analytics-service    | 8088 | PostgreSQL | Revenue, rides, driver earnings    |

## Kafka Event Flow

```
User books ride
    └─► booking-service publishes → [ride-requested]
            ├─► trip-service (creates Trip record)
            └─► notification-service → RabbitMQ

Driver accepts
    └─► booking-service publishes → [driver-accepted]
            └─► trip-service (assigns driver to trip)

Driver starts ride
    └─► trip-service publishes → [ride-started]

Trip ends
    └─► trip-service publishes → [ride-completed]
            ├─► notification-service → RabbitMQ
            └─► analytics-service (stores to PostgreSQL)

Payment processed
    └─► payment-service publishes → [payment-completed]
            └─► notification-service → RabbitMQ
```

## Redis Geo — Driver Matching

Driver goes ONLINE → location stored in Redis GEO set `driver_locations`

```
GET /api/drivers/nearby?lat=18.5204&lon=73.8567&radius=3.0
```

Internally uses `GEOSEARCH driver_locations FROMLONLAT 73.8567 18.5204 BYRADIUS 3 km ASC COUNT 5`

## Pricing Logic

| Factor       | Value      |
|--------------|------------|
| Base fare    | ₹50        |
| Per km       | ₹12        |
| Peak hours   | 8–10:30am, 5:30–8pm → 1.5x |
| Night charge | 10pm–6am → +20%            |

```
GET /api/pricing/calculate?distanceKm=8.5
```

## Quick Start

### With Docker Compose

```bash
docker-compose up -d
```

Services auto-start after MySQL/Postgres health checks pass.

### Without Docker (local dev)

Start infrastructure:
```bash
docker-compose up -d mysql postgres redis zookeeper kafka rabbitmq
```

Then run each service:
```bash
cd user-service && mvn spring-boot:run
cd driver-service && mvn spring-boot:run
# ... repeat for each service
```

## Swagger UI

Each service exposes Swagger at `http://localhost:{port}/swagger-ui.html`

| Service          | Swagger URL                              |
|------------------|------------------------------------------|
| user-service     | http://localhost:8081/swagger-ui.html    |
| driver-service   | http://localhost:8082/swagger-ui.html    |
| booking-service  | http://localhost:8083/swagger-ui.html    |
| trip-service     | http://localhost:8084/swagger-ui.html    |
| payment-service  | http://localhost:8085/swagger-ui.html    |
| pricing-service  | http://localhost:8086/swagger-ui.html    |
| analytics-service| http://localhost:8088/swagger-ui.html    |

## Sample Booking Flow

```bash
# 1. Register user
POST http://localhost:8081/api/users/register
{"name":"Alice","email":"alice@ridex.com","password":"pass123","phone":"9999999999"}

# 2. Login → get JWT
POST http://localhost:8081/api/users/login
{"email":"alice@ridex.com","password":"pass123"}

# 3. Register driver
POST http://localhost:8082/api/drivers/register
{"name":"Bob Driver","licenseNumber":"MH12AB1234","vehicleType":"SEDAN","email":"bob@ridex.com"}

# 4. Driver goes online with location
PUT http://localhost:8082/api/drivers/status/1
{"status":"ONLINE","latitude":18.5204,"longitude":73.8567}

# 5. Find nearest drivers
GET http://localhost:8082/api/drivers/nearby?lat=18.5200&lon=73.8560&radius=3.0

# 6. Calculate fare
GET http://localhost:8086/api/pricing/calculate?distanceKm=8.5

# 7. Create booking
POST http://localhost:8083/api/bookings/create
{"userId":1,"pickupLocation":"Pune Station","dropLocation":"Airport","estimatedFare":152.0}

# 8. Assign driver
PUT http://localhost:8083/api/bookings/assign-driver/1?driverId=1

# 9. Start trip
PUT http://localhost:8084/api/trips/start/1

# 10. End trip
PUT http://localhost:8084/api/trips/end/1

# 11. Process payment
POST http://localhost:8085/api/payment/pay
{"bookingId":1,"userId":1,"amount":152.0,"paymentMethod":"UPI"}

# 12. View analytics
GET http://localhost:8088/api/analytics/rides/today
GET http://localhost:8088/api/analytics/revenue/today
```

## Security

- JWT authentication in user-service
- Token format: `Authorization: Bearer <token>`
- Roles: USER, DRIVER, ADMIN (extend per service as needed)

## Tech Stack

- **Spring Boot 3.2** — All microservices
- **MySQL 8** — Transactional data (users, drivers, bookings, trips, payments)
- **PostgreSQL 15** — Analytics/reporting
- **Redis 7** — Driver geospatial indexing (`GEOSEARCH`)
- **Apache Kafka** — Async event streaming (ride lifecycle events)
- **RabbitMQ 3** — Notification queue (email/SMS)
- **Springdoc OpenAPI** — Swagger UI per service
- **Docker Compose** — Full local stack

## Folder Structure

```
RideX/
├── common-library/          # Shared DTOs and enums
├── user-service/            # :8081 — Rider accounts + JWT
├── driver-service/          # :8082 — Drivers + Redis GEO
├── booking-service/         # :8083 — Core booking + Kafka producer
├── trip-service/            # :8084 — Trip lifecycle + Kafka consumer
├── payment-service/         # :8085 — Payments + Kafka
├── pricing-service/         # :8086 — Dynamic fare calculator
├── notification-service/    # :8087 — Kafka→RabbitMQ bridge
├── analytics-service/       # :8088 — PostgreSQL analytics
├── docker-compose.yml
└── README.md
```
