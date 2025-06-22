package com.lukianchykov.bookingsystem.dto;

import com.lukianchykov.bookingsystem.domain.AccommodationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitUpdateRequest {

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "Number of rooms must be at least 1")
    private Integer numberOfRooms;

    @NotNull(message = "Accommodation type is required")
    private AccommodationType accommodationType;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor must be non-negative")
    private Integer floor;

    @NotNull(message = "Base cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base cost must be positive")
    private BigDecimal baseCost;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Final cost must be positive")
    private BigDecimal finalCost;

    @NotNull(message = "Description is required")
    @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters")
    private String description;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;
}