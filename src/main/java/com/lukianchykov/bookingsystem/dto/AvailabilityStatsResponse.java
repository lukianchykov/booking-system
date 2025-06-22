package com.lukianchykov.bookingsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AvailabilityStatsResponse {

    @JsonProperty("available_units")
    private Long availableUnits;

    @JsonProperty("total_units")
    private Long totalUnits;

    @JsonProperty("availability_percentage")
    private Double availabilityPercentage;
}