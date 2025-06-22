package com.lukianchykov.bookingsystem.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lukianchykov.bookingsystem.domain.AccommodationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitCreateRequest {

    @JsonProperty("number_of_rooms")
    private Integer numberOfRooms;

    @JsonProperty("accommodation_type")
    private AccommodationType accommodationType;

    private Integer floor;

    @JsonProperty("base_cost")
    @NotNull
    private BigDecimal baseCost;

    private String description;

    @JsonProperty("owner_id")
    private Long ownerId;
}
