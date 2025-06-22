package com.lukianchykov.bookingsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lukianchykov.bookingsystem.domain.AccommodationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitSearchRequest {

    @JsonProperty("number_of_rooms")
    private Integer numberOfRooms;

    @JsonProperty("accommodation_type")
    private AccommodationType accommodationType;

    private Integer floor;

    @JsonProperty("min_cost")
    private BigDecimal minCost;

    @JsonProperty("max_cost")
    private BigDecimal maxCost;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("sort_by")
    private String sortBy = "id";

    @JsonProperty("sort_direction")
    private String sortDirection = "ASC";

    private Integer page = 0;

    private Integer size = 10;
}
