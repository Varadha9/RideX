package com.ridex.driver.service;

import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverLocationService {

    private static final String GEO_KEY = "driver_locations";
    private final RedisTemplate<String, String> redisTemplate;

    public DriverLocationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateLocation(Long driverId, double latitude, double longitude) {
        GeoOperations<String, String> geo = redisTemplate.opsForGeo();
        geo.add(GEO_KEY, new Point(longitude, latitude), "driver_" + driverId);
    }

    public void removeDriver(Long driverId) {
        redisTemplate.opsForGeo().remove(GEO_KEY, "driver_" + driverId);
    }

    public List<Long> findNearestDrivers(double latitude, double longitude, double radiusKm, int limit) {
        GeoOperations<String, String> geo = redisTemplate.opsForGeo();
        Circle circle = new Circle(new Point(longitude, latitude),
                new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs().includeDistance().sortAscending().limit(limit);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geo.radius(GEO_KEY, circle, args);
        if (results == null) return List.of();
        return results.getContent().stream()
                .map(r -> Long.parseLong(r.getContent().getName().replace("driver_", "")))
                .collect(Collectors.toList());
    }
}
