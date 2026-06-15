# analytics-service

Consumes `ride-completed` events from Kafka and stores them in PostgreSQL for business reporting — revenue, ride counts, and driver earnings.

- **Port:** 8088
- **Database:** PostgreSQL (`ridex_analytics`)
- **Swagger:** http://localhost:8088/swagger-ui.html

## Endpoints

| Method | Endpoint                              | Description                  |
|--------|---------------------------------------|------------------------------|
| GET    | /api/analytics/rides/today            | Total rides completed today  |
| GET    | /api/analytics/revenue/today          | Total revenue today          |
| GET    | /api/analytics/driver/earnings/{id}   | Total earnings for a driver  |
| GET    | /api/analytics/recent                 | Last 10 completed rides      |

## Kafka Events Consumed

| Topic          | Action                                  |
|----------------|-----------------------------------------|
| ride-completed | Stores RideAnalytics record in PostgreSQL |

## RideAnalytics Schema

| Field      | Type      | Description                   |
|------------|-----------|-------------------------------|
| id         | Long      | Primary key                   |
| bookingId  | Long      | Reference to booking          |
| userId     | Long      | Rider ID                      |
| driverId   | Long      | Driver ID                     |
| fare       | Double    | Final fare paid               |
| timestamp  | DateTime  | When the ride completed       |

## Example Queries

```bash
# Total rides today
curl http://localhost:8088/api/analytics/rides/today

# Revenue today
curl http://localhost:8088/api/analytics/revenue/today

# Driver earnings
curl http://localhost:8088/api/analytics/driver/earnings/1

# Recent rides
curl http://localhost:8088/api/analytics/recent
```

## Sample Response

```json
{ "date": "2025-06-15", "totalRides": 42 }
{ "date": "2025-06-15", "totalRevenue": 5824.50 }
{ "driverId": 1, "totalEarnings": 3200.00 }
```
