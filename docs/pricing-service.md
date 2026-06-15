# pricing-service

Stateless dynamic fare calculator. No database — pure logic based on distance, time of day, and configured multipliers.

- **Port:** 8086
- **Database:** None
- **Swagger:** http://localhost:8086/swagger-ui.html

## Endpoints

| Method | Endpoint                           | Description         |
|--------|------------------------------------|---------------------|
| GET    | /api/pricing/calculate?distanceKm  | Calculate ride fare |

## Pricing Formula

```
fare = baseFare + (perKmRate × distanceKm)

if peak hours  → fare × 1.5
if night time  → fare + (fare × 0.20)
```

## Configuration (application.yml)

```yaml
pricing:
  base-fare: 50.0
  per-km-rate: 12.0
  peak-hour-multiplier: 1.5
  night-charge-percent: 20.0
```

## Peak Hours

| Window         | Time               |
|----------------|--------------------|
| Morning peak   | 8:00 AM – 10:30 AM |
| Evening peak   | 5:30 PM – 8:00 PM  |

## Night Hours

10:00 PM – 6:00 AM

## Example

```bash
curl "http://localhost:8086/api/pricing/calculate?distanceKm=8.5"
```

Response:
```json
{
  "distanceKm": 8.5,
  "baseFare": 50.0,
  "totalFare": 152.0,
  "isPeakHour": false,
  "isNightCharge": false
}
```
