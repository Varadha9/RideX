# booking-service

Core booking management. Creates rides, assigns drivers, and publishes Kafka events for the rest of the system to react to.

- **Port:** 8083
- **Database:** MySQL (`ridex`) + Kafka producer
- **Swagger:** http://localhost:8083/swagger-ui.html

## Endpoints

| Method | Endpoint                          | Description          |
|--------|-----------------------------------|----------------------|
| POST   | /api/bookings/create              | Create a booking     |
| GET    | /api/bookings/status/{id}         | Get booking status   |
| POST   | /api/bookings/cancel/{id}         | Cancel a booking     |
| PUT    | /api/bookings/assign-driver/{id}  | Assign a driver      |

## Booking Status Flow

```
REQUESTED → DRIVER_ASSIGNED → STARTED → COMPLETED
                           ↘ CANCELLED
```

## Kafka Events Published

| Action          | Topic            | Payload                          |
|-----------------|------------------|----------------------------------|
| Booking created | ride-requested   | bookingId, userId, pickup, drop  |
| Driver assigned | driver-accepted  | bookingId, driverId              |
| Booking cancelled| ride-requested  | eventType: CANCELLED             |

## Create Booking Example

```bash
curl -X POST http://localhost:8083/api/bookings/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "pickupLocation": "Pune Station",
    "dropLocation": "Airport",
    "estimatedFare": 152.0
  }'
```

## Assign Driver Example

```bash
curl -X PUT "http://localhost:8083/api/bookings/assign-driver/1?driverId=1"
```
