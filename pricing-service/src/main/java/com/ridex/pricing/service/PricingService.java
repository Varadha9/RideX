package com.ridex.pricing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Map;

@Service
public class PricingService {

    @Value("${pricing.base-fare}") private double baseFare;
    @Value("${pricing.per-km-rate}") private double perKmRate;
    @Value("${pricing.peak-hour-multiplier}") private double peakMultiplier;
    @Value("${pricing.night-charge-percent}") private double nightChargePercent;

    public Map<String, Object> calculateFare(double distanceKm) {
        double fare = baseFare + (perKmRate * distanceKm);

        boolean isPeakHour = isPeakHour();
        boolean isNight = isNightTime();

        if (isPeakHour) fare *= peakMultiplier;
        if (isNight) fare += fare * (nightChargePercent / 100);

        return Map.of(
                "distanceKm", distanceKm,
                "baseFare", baseFare,
                "totalFare", Math.round(fare * 100.0) / 100.0,
                "isPeakHour", isPeakHour,
                "isNightCharge", isNight
        );
    }

    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(10, 30))) ||
               (now.isAfter(LocalTime.of(17, 30)) && now.isBefore(LocalTime.of(20, 0)));
    }

    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0));
    }
}
