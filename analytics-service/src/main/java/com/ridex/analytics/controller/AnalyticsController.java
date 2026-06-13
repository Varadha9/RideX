package com.ridex.analytics.controller;

import com.ridex.analytics.entity.RideAnalytics;
import com.ridex.analytics.repository.AnalyticsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics Service", description = "Reporting and business metrics (PostgreSQL)")
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @GetMapping("/rides/today")
    @Operation(summary = "Total rides today")
    public ResponseEntity<Map<String, Object>> ridesToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        Long count = analyticsRepository.countRidesInRange(start, start.plusDays(1));
        return ResponseEntity.ok(Map.of("date", LocalDate.now().toString(), "totalRides", count != null ? count : 0L));
    }

    @GetMapping("/revenue/today")
    @Operation(summary = "Total revenue today")
    public ResponseEntity<Map<String, Object>> revenueToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        Double revenue = analyticsRepository.sumRevenueInRange(start, start.plusDays(1));
        return ResponseEntity.ok(Map.of("date", LocalDate.now().toString(), "totalRevenue", revenue != null ? revenue : 0.0));
    }

    @GetMapping("/driver/earnings/{driverId}")
    @Operation(summary = "Total earnings for a driver")
    public ResponseEntity<Map<String, Object>> driverEarnings(@PathVariable Long driverId) {
        Double earnings = analyticsRepository.driverEarnings(driverId);
        return ResponseEntity.ok(Map.of("driverId", driverId, "totalEarnings", earnings != null ? earnings : 0.0));
    }

    @GetMapping("/recent")
    @Operation(summary = "Last 10 completed rides")
    public ResponseEntity<List<RideAnalytics>> recent() {
        return ResponseEntity.ok(analyticsRepository.findTop10ByOrderByTimestampDesc());
    }
}
