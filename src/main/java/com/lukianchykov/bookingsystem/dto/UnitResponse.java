package com.lukianchykov.bookingsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lukianchykov.bookingsystem.domain.AccommodationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {

    private Long id;

    @JsonProperty("number_of_rooms")
    private Integer numberOfRooms;

    @JsonProperty("accommodation_type")
    private AccommodationType accommodationType;

    private Integer floor;

    @JsonProperty("base_cost")
    @NotNull
    private BigDecimal baseCost;

    @JsonProperty("final_cost")
    @NotNull
    private BigDecimal finalCost;

    private String description;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private Boolean available;
}
