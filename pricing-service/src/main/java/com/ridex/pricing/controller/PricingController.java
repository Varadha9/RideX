package com.ridex.pricing.controller;

import com.ridex.pricing.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@Tag(name = "Pricing Service", description = "Dynamic fare calculation")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate fare — base ₹50 + ₹12/km, peak 1.5x, night +20%")
    public ResponseEntity<Map<String, Object>> calculate(@RequestParam double distanceKm) {
        return ResponseEntity.ok(pricingService.calculateFare(distanceKm));
    }
}
