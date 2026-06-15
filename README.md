# RideX — Production-Grade Cab Booking Platform

> A full microservices backend inspired by Uber/Ola, built with Spring Boot 3.5, Kafka, Redis GEO, RabbitMQ, MySQL, PostgreSQL — all running in Docker.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Kafka Event Flow](#kafka-event-flow)
- [Redis GEO — Driver Matching](#redis-geo--driver-matching)
- [Pricing Logic](#pricing-logic)
- [API Reference](#api-reference)
- [Sample End-to-End Flow](#sample-end-to-end-flow)
- [Swagger UI](#swagger-ui)
- [Project Structure](#project-structure)
- [Environment Variables](#environment-variables)
- [Memory & Performance Notes](#memory--performance-notes)
- [Makefile Commands](#makefile-commands)
- [Troubleshooting](#troubleshooting)

---

## Overview

RideX is a backend system that simulates a real-world cab booking platform. It is broken into 8 independent microservices that communicate via REST, Kafka event streams, and RabbitMQ queues.

Key engineering concepts demonstrated:
- **Microservices** with independent databases per service
- **Event-driven architecture** using Apache Kafka for ride lifecycle events
- **Geospatial indexing** using Redis GEO for nearest driver lookup
- **JWT authentication** with Spring Security
- **Dynamic pricing** with peak hour and night charge multipliers
- **Message bridging** from Kafka to RabbitMQ for notifications
- **Analytics** stored in PostgreSQL, consumed from Kafka

---

## Architecture

```
Client (Mobile / Web)
          │
          ▼
  [ API Gateway ]  ← future: Spring Cloud Gateway
          │
  ┌───────┴────────────────────────────────────────┐
  │  user      driver    booking   trip   payment  │
  │  :8081     :8082     :8083    :8084   :8085    │
  │                                                │
  │  pricing   notification       analytics        │
  │  :8086     :8087              :8088            │
  └───────────────────────────────────────────────-┘
          │                    │
       [Kafka]            [RabbitMQ]
          │
      [Redis GEO]
          │
    [MySQL]  [PostgreSQL]
```

---

## Services

| Service              | Port | Database        | Responsibility                              |
|----------------------|------|-----------------|---------------------------------------------|
| user-service         | 8081 | MySQL           | Rider registration, login, JWT auth         |
| driver-service       | 8082 | MySQL + Redis   | Driver CRUD, Redis GEO location tracking    |
| booking-service      | 8083 | MySQL + Kafka   | Create/cancel bookings, publish ride events |
| trip-service         | 8084 | MySQL + Kafka   | Trip lifecycle (start/end), Kafka consumer  |
| payment-service      | 8085 | MySQL + Kafka   | WALLET / UPI / CARD payment simulation      |
| pricing-service      | 8086 | —               | Dynamic fare: peak hours + night surcharge  |
| notification-service | 8087 | Kafka + RabbitMQ| Kafka → RabbitMQ bridge for notifications   |
| analytics-service    | 8088 | PostgreSQL      | Revenue, ride count, driver earnings        |

---

## Tech Stack

| Technology          | Version | Purpose                              |
|---------------------|---------|--------------------------------------|
| Spring Boot         | 3.5.0   | All microservices framework          |
| Java                | 25      | Language                             |
| MySQL               | 8.0     | Transactional data (users, bookings) |
| PostgreSQL          | 15      | Analytics / reporting                |
| Redis               | 7       | Driver geospatial indexing (GEO)     |
| Apache Kafka        | 7.4.0   | Async event streaming                |
| RabbitMQ            | 3       | Notification delivery queue          |
| Springdoc OpenAPI   | 2.2.0   | Swagger UI per service               |
| Docker Compose      | —       | Full local stack orchestration       |
| Maven               | 3.x     | Build tool                           |

---

## Prerequisites

- **Docker** + **Docker Compose** (v2+)
- **Java 25** (for building JARs locally)
- **Maven 3.8+**
- Minimum **8GB RAM** recommended (14 containers run simultaneously)

---

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/Varadha9/RideX.git
cd RideX
```

### 2. Build all JARs

```bash
make build
```

This compiles the `common-library` first, then packages all 8 services into fat JARs.

### 3. Start all containers

```bash
make up
```

Or equivalently:

```bash
docker-compose up -d --build
```

Docker builds images for all 8 services and starts all 14 containers (8 services + MySQL + PostgreSQL + Redis + Kafka + Zookeeper + RabbitMQ).

### 4. Wait ~2 minutes

Spring Boot services with JPA + Kafka take ~60–90 seconds to fully initialize. You can watch progress with:

```bash
docker-compose logs -f user-service
```

### 5. Open Swagger UI

```
http://localhost:8081/swagger-ui.html
```

---

## Kafka Event Flow

```
User books ride
    └─► booking-service publishes → [ride-requested]
            ├─► trip-service       (creates Trip record in MySQL)
            └─► notification-service → RabbitMQ → notification_queue

Driver assigned
    └─► booking-service publishes → [driver-accepted]
            └─► trip-service       (links driver to trip)

Driver starts ride
    └─► trip-service publishes → [ride-started]

Trip ends
    └─► trip-service publishes → [ride-completed]
            ├─► notification-service → RabbitMQ
            └─► analytics-service   (stores record in PostgreSQL)

Payment processed
    └─► payment-service publishes → [payment-completed]
            └─► notification-service → RabbitMQ
```

### Kafka Topics

| Topic              | Producer         | Consumers                               |
|--------------------|------------------|-----------------------------------------|
| ride-requested     | booking-service  | trip-service, notification-service      |
| driver-accepted    | booking-service  | trip-service, notification-service      |
| ride-started       | trip-service     | notification-service                    |
| ride-completed     | trip-service     | analytics-service, notification-service |
| payment-completed  | payment-service  | notification-service                    |

---

## Redis GEO — Driver Matching

When a driver goes **ONLINE**, their location is stored in a Redis GEO set:

```
Key: driver_locations
Value: driver_{id} → (longitude, latitude)
```

To find drivers near a rider:

```
GET /api/drivers/nearby?lat=18.5204&lon=73.8567&radius=3.0
```

Internally executes:
```
GEOSEARCH driver_locations FROMLONLAT 73.8567 18.5204 BYRADIUS 3 km ASC COUNT 5
```

Returns a list of driver IDs sorted by distance ascending.

---

## Pricing Logic

```
Fare = baseFare + (perKmRate × distanceKm)

If peak hours  → Fare × 1.5
If night time  → Fare + 20%
```

| Factor            | Value                          |
|-------------------|--------------------------------|
| Base fare         | ₹50                            |
| Per km rate       | ₹12                            |
| Peak hours        | 8:00–10:30 AM, 5:30–8:00 PM    |
| Peak multiplier   | 1.5×                           |
| Night hours       | 10:00 PM – 6:00 AM             |
| Night surcharge   | +20%                           |

Example:
```
GET /api/pricing/calculate?distanceKm=8.5

Response:
{
  "distanceKm": 8.5,
  "baseFare": 50.0,
  "totalFare": 152.0,
  "isPeakHour": false,
  "isNightCharge": false
}
```

---

## API Reference

### user-service — :8081

| Method | Endpoint                  | Description              | Auth     |
|--------|---------------------------|--------------------------|----------|
| POST   | /api/users/register       | Register a new rider     | Public   |
| POST   | /api/users/login          | Login, returns JWT token | Public   |
| GET    | /api/users/profile        | Get current user profile | JWT      |
| PUT    | /api/users/update/{id}    | Update profile           | JWT      |

### driver-service — :8082

| Method | Endpoint                    | Description                     | Auth   |
|--------|-----------------------------|---------------------------------|--------|
| POST   | /api/drivers/register       | Register a new driver           | Public |
| GET    | /api/drivers/profile/{id}   | Get driver profile              | Public |
| PUT    | /api/drivers/status/{id}    | Update status + location        | Public |
| GET    | /api/drivers/nearby         | Find nearest drivers (Redis GEO)| Public |
| PUT    | /api/drivers/rating/{id}    | Update driver rating            | Public |

### booking-service — :8083

| Method | Endpoint                              | Description             |
|--------|---------------------------------------|-------------------------|
| POST   | /api/bookings/create                  | Create a booking        |
| GET    | /api/bookings/status/{id}             | Get booking status      |
| POST   | /api/bookings/cancel/{id}             | Cancel a booking        |
| PUT    | /api/bookings/assign-driver/{id}      | Assign driver           |

### trip-service — :8084

| Method | Endpoint                          | Description              |
|--------|-----------------------------------|--------------------------|
| PUT    | /api/trips/start/{bookingId}      | Start trip               |
| PUT    | /api/trips/end/{bookingId}        | End trip                 |
| GET    | /api/trips/history/user/{userId}  | User trip history        |
| GET    | /api/trips/history/driver/{id}    | Driver trip history      |

### payment-service — :8085

| Method | Endpoint                     | Description                   |
|--------|------------------------------|-------------------------------|
| POST   | /api/payment/pay             | Process payment               |
| GET    | /api/payment/history/{userId}| Get payment history           |

### pricing-service — :8086

| Method | Endpoint                          | Description        |
|--------|-----------------------------------|--------------------|
| GET    | /api/pricing/calculate?distanceKm | Calculate fare     |

### analytics-service — :8088

| Method | Endpoint                            | Description              |
|--------|-------------------------------------|--------------------------|
| GET    | /api/analytics/rides/today          | Total rides today        |
| GET    | /api/analytics/revenue/today        | Total revenue today      |
| GET    | /api/analytics/driver/earnings/{id} | Driver total earnings    |
| GET    | /api/analytics/recent               | Last 10 completed rides  |

---

## Sample End-to-End Flow

```bash
# 1. Register a rider
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@ridex.com","password":"pass123","phone":"9999999999"}'

# 2. Login — copy the token from response
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@ridex.com","password":"pass123"}'

# 3. Register a driver
curl -X POST http://localhost:8082/api/drivers/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","licenseNumber":"MH12AB1234","vehicleType":"SEDAN","email":"bob@ridex.com"}'

# 4. Driver goes ONLINE with location
curl -X PUT http://localhost:8082/api/drivers/status/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"ONLINE","latitude":18.5204,"longitude":73.8567}'

# 5. Find nearby drivers
curl "http://localhost:8082/api/drivers/nearby?lat=18.5200&lon=73.8560&radius=3.0"

# 6. Calculate fare
curl "http://localhost:8086/api/pricing/calculate?distanceKm=8.5"

# 7. Create booking
curl -X POST http://localhost:8083/api/bookings/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"pickupLocation":"Pune Station","dropLocation":"Airport","estimatedFare":152.0}'

# 8. Assign driver to booking
curl -X PUT "http://localhost:8083/api/bookings/assign-driver/1?driverId=1"

# 9. Start trip
curl -X PUT http://localhost:8084/api/trips/start/1

# 10. End trip
curl -X PUT http://localhost:8084/api/trips/end/1

# 11. Process payment
curl -X POST http://localhost:8085/api/payment/pay \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1,"userId":1,"amount":152.0,"paymentMethod":"UPI"}'

# 12. View analytics
curl http://localhost:8088/api/analytics/rides/today
curl http://localhost:8088/api/analytics/revenue/today
```

---

## Swagger UI

Each service has a full Swagger UI:

| Service              | URL                                       |
|----------------------|-------------------------------------------|
| user-service         | http://localhost:8081/swagger-ui.html     |
| driver-service       | http://localhost:8082/swagger-ui.html     |
| booking-service      | http://localhost:8083/swagger-ui.html     |
| trip-service         | http://localhost:8084/swagger-ui.html     |
| payment-service      | http://localhost:8085/swagger-ui.html     |
| pricing-service      | http://localhost:8086/swagger-ui.html     |
| analytics-service    | http://localhost:8088/swagger-ui.html     |

> **Tip:** For user-service endpoints that require auth, click **Authorize** and enter `Bearer <token>` after logging in.

---

## Project Structure

```
RideX/
├── common-library/          # Shared DTOs, enums, Kafka event classes
│   └── src/main/java/com/ridex/common/
│       ├── enums/           # BookingStatus, PaymentMethod
│       └── events/          # RideEvent, NotificationEvent
│
├── user-service/            # :8081 — JWT auth + rider accounts
├── driver-service/          # :8082 — Driver CRUD + Redis GEO
├── booking-service/         # :8083 — Booking CRUD + Kafka producer
├── trip-service/            # :8084 — Trip lifecycle + Kafka consumer
├── payment-service/         # :8085 — Payment processing + Kafka
├── pricing-service/         # :8086 — Dynamic fare calculation
├── notification-service/    # :8087 — Kafka → RabbitMQ bridge
├── analytics-service/       # :8088 — PostgreSQL analytics
│
├── docker-compose.yml       # Full infrastructure + service definitions
├── Makefile                 # build / up / down / restart shortcuts
└── README.md
```

Each service follows the same internal structure:
```
{service}/
├── src/main/java/com/ridex/{service}/
│   ├── {Service}Application.java   # Spring Boot entry point
│   ├── controller/                 # REST controllers
│   ├── service/                    # Business logic
│   ├── entity/                     # JPA entities
│   ├── repository/                 # Spring Data JPA repos
│   └── config/                     # Spring config beans
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

---

## Environment Variables

All environment variables are set in `docker-compose.yml`. Override them per-service:

| Variable                       | Default                              | Used By                        |
|--------------------------------|--------------------------------------|--------------------------------|
| SPRING_DATASOURCE_URL          | jdbc:mysql://mysql:3306/ridex        | user, driver, booking, trip, payment |
| SPRING_DATASOURCE_USERNAME     | root                                 | MySQL services                 |
| SPRING_DATASOURCE_PASSWORD     | root                                 | MySQL services                 |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | kafka:29092                          | booking, trip, payment, notification, analytics |
| SPRING_REDIS_HOST              | redis                                | driver-service                 |
| SPRING_RABBITMQ_HOST           | rabbitmq                             | notification-service           |
| SPRING_RABBITMQ_USERNAME       | ridex                                | notification-service           |
| SPRING_RABBITMQ_PASSWORD       | ridex                                | notification-service           |

---

## Memory & Performance Notes

Running 14 containers simultaneously requires significant RAM. Optimizations already applied:

- `spring.main.lazy-initialization: true` — reduces startup heap usage by ~40%
- `KAFKA_HEAP_OPTS: -Xmx256M -Xms128M` — caps Kafka JVM heap
- `ZOOKEEPER_HEAP_OPTS: -Xmx128M -Xms64M` — caps Zookeeper heap
- `--innodb-buffer-pool-size=128M` on MySQL — reduces buffer pool
- `mem_limit: 384m` on each Spring Boot service
- `mem_limit: 768m` + `memswap_limit: 1536m` on Kafka
- `restart: on-failure` on all services — auto-recovers from OOM kills

**Recommended:** 8GB+ RAM. If Kafka gets OOM-killed:
```bash
docker-compose restart kafka
sleep 30
docker-compose start trip-service analytics-service
```

---

## Makefile Commands

```bash
make build      # Build common-library + all 8 service JARs
make up         # docker-compose up -d --build
make down       # docker-compose down
make restart    # Full rebuild + restart
make logs       # Follow all container logs
make status     # Show running containers with ports
```

---

## Troubleshooting

**Swagger shows "Failed to fetch /api-docs"**
→ Spring Security was blocking the endpoint. Already fixed — `/api-docs` is in the permit list.

**ERR_CONNECTION_REFUSED on localhost:808x**
→ A service got OOM-killed. Run: `docker-compose start <service-name>`

**Kafka "No resolvable bootstrap urls"**
→ Kafka container is down. Run: `docker-compose restart kafka && sleep 30`

**MySQL container exits with code 137**
→ OOM kill. Free up RAM or ensure the swapfile is active:
```bash
sudo swapon /swapfile2
docker-compose restart mysql
```

**"Failed to construct kafka consumer" on startup**
→ Trip/analytics services started before Kafka was ready. They have `restart: on-failure` and will retry automatically.
