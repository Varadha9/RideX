package com.ridex.driver.controller;

import com.ridex.driver.entity.Driver;
import com.ridex.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Service", description = "Driver management and location")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new driver")
    public ResponseEntity<Driver> register(@RequestBody Driver driver) {
        return ResponseEntity.ok(driverService.register(driver));
    }

    @GetMapping("/profile/{id}")
    @Operation(summary = "Get driver profile")
    public ResponseEntity<Driver> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getProfile(id));
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Update driver status and location")
    public ResponseEntity<Driver> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Driver.DriverStatus status = Driver.DriverStatus.valueOf((String) body.get("status"));
        Double lat = body.get("latitude") != null ? ((Number) body.get("latitude")).doubleValue() : null;
        Double lon = body.get("longitude") != null ? ((Number) body.get("longitude")).doubleValue() : null;
        return ResponseEntity.ok(driverService.updateStatus(id, status, lat, lon));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find nearest drivers via Redis GEO")
    public ResponseEntity<List<Long>> getNearby(@RequestParam double lat,
                                                @RequestParam double lon,
                                                @RequestParam(defaultValue = "3.0") double radius) {
        return ResponseEntity.ok(driverService.getNearestDrivers(lat, lon, radius));
    }

    @PutMapping("/rating/{id}")
    @Operation(summary = "Update driver rating")
    public ResponseEntity<Driver> updateRating(@PathVariable Long id, @RequestParam double rating) {
        return ResponseEntity.ok(driverService.updateRating(id, rating));
    }
}
