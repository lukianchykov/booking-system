package com.lukianchykov.bookingsystem.controller;

import com.lukianchykov.bookingsystem.dto.UnitCreateRequest;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import com.lukianchykov.bookingsystem.dto.UnitSearchRequest;
import com.lukianchykov.bookingsystem.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Tag(name = "Units", description = "Unit management operations")
public class UnitController {
    
    private final UnitService unitService;
    
    @PostMapping
    public ResponseEntity<UnitResponse> createUnit(@RequestBody UnitCreateRequest request) {
        UnitResponse response = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search available units")
    public ResponseEntity<Page<UnitResponse>> searchUnits(
            @ModelAttribute UnitSearchRequest request) {
        Page<UnitResponse> response = unitService.searchUnits(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get unit by ID")
    public ResponseEntity<UnitResponse> getUnit(@PathVariable Long id) {
        UnitResponse response = unitService.getUnit(id);
        return ResponseEntity.ok(response);
    }
}