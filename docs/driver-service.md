# driver-service

Manages driver accounts and real-time geospatial location tracking using Redis GEO.

- **Port:** 8082
- **Database:** MySQL (`ridex`) + Redis
- **Swagger:** http://localhost:8082/swagger-ui.html

## Endpoints

| Method | Endpoint                  | Description                      |
|--------|---------------------------|----------------------------------|
| POST   | /api/drivers/register     | Register a new driver            |
| GET    | /api/drivers/profile/{id} | Get driver profile               |
| PUT    | /api/drivers/status/{id}  | Update status + GPS location     |
| GET    | /api/drivers/nearby       | Find nearest drivers (Redis GEO) |
| PUT    | /api/drivers/rating/{id}  | Update driver rating             |

## Driver Status

`OFFLINE` → `ONLINE` → `ON_TRIP` → `OFFLINE`

When a driver goes `ONLINE`, their location is stored in Redis:
```
GEOADD driver_locations <longitude> <latitude> driver_<id>
```

When they go `OFFLINE`, they are removed from the GEO set.

## Find Nearby Drivers

```bash
curl "http://localhost:8082/api/drivers/nearby?lat=18.5204&lon=73.8567&radius=3.0"
```

Returns driver IDs sorted by distance (closest first). Internally uses:
```
GEOSEARCH driver_locations FROMLONLAT 73.8567 18.5204 BYRADIUS 3 km ASC COUNT 5
```

## Update Status + Location Example

```bash
curl -X PUT http://localhost:8082/api/drivers/status/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"ONLINE","latitude":18.5204,"longitude":73.8567}'
```
