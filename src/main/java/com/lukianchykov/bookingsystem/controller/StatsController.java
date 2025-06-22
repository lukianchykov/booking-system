package com.lukianchykov.bookingsystem.controller;

import java.util.Map;

import com.lukianchykov.bookingsystem.dto.AvailabilityStatsResponse;
import com.lukianchykov.bookingsystem.service.CacheService;
import com.lukianchykov.bookingsystem.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics operations")
public class StatsController {

    private final CacheService cacheService;

    private final UnitService unitService;

    @GetMapping("/availability")
    @Operation(summary = "Get availability statistics")
    public ResponseEntity<AvailabilityStatsResponse> getAvailabilityStats() {
        Long availableUnits = cacheService.getAvailableUnitsCount();
        Long totalUnits = unitService.countTotalUnits();

        AvailabilityStatsResponse response = new AvailabilityStatsResponse();
        response.setAvailableUnits(availableUnits);
        response.setTotalUnits(totalUnits);

        if (totalUnits > 0) {
            response.setAvailabilityPercentage((double) availableUnits / totalUnits * 100);
        } else {
            response.setAvailabilityPercentage(0.0);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-units")
    @Operation(summary = "Get number of available units for booking (cached)")
    public ResponseEntity<Long> getAvailableUnitsCount() {
        Long count = cacheService.getAvailableUnitsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/cache-health")
    @Operation(summary = "Check cache health status")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        boolean isHealthy = cacheService.isCacheHealthy();
        return ResponseEntity.ok(Map.of(
            "healthy", isHealthy,
            "status", isHealthy ? "UP" : "DOWN"
        ));
    }
}