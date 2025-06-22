package com.lukianchykov.bookingsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("payment_method")
    private String paymentMethod;
}