package com.ridex.trip.controller;

import com.ridex.trip.entity.Trip;
import com.ridex.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trip Service", description = "Trip lifecycle management")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PutMapping("/start/{bookingId}")
    @Operation(summary = "Start a trip")
    public ResponseEntity<Trip> start(@PathVariable Long bookingId) {
        return ResponseEntity.ok(tripService.startTrip(bookingId));
    }

    @PutMapping("/end/{bookingId}")
    @Operation(summary = "End a trip and publish ride-completed event")
    public ResponseEntity<Trip> end(@PathVariable Long bookingId) {
        return ResponseEntity.ok(tripService.endTrip(bookingId));
    }

    @GetMapping("/history/user/{userId}")
    @Operation(summary = "Get trip history for a user")
    public ResponseEntity<List<Trip>> userHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(tripService.getUserHistory(userId));
    }

    @GetMapping("/history/driver/{driverId}")
    @Operation(summary = "Get trip history for a driver")
    public ResponseEntity<List<Trip>> driverHistory(@PathVariable Long driverId) {
        return ResponseEntity.ok(tripService.getDriverHistory(driverId));
    }
}
