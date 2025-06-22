package com.lukianchykov.bookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukianchykov.bookingsystem.controller.exception.BookingNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.PaymentProcessingException;
import com.lukianchykov.bookingsystem.controller.handler.GlobalExceptionHandler;
import com.lukianchykov.bookingsystem.dto.PaymentRequest;
import com.lukianchykov.bookingsystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .bookingId(1L)
                .paymentMethod("CREDIT_CARD")
                .build();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        doNothing().when(paymentService).processPayment(any(PaymentRequest.class));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment processed successfully"));

        verify(paymentService, times(1)).processPayment(any(PaymentRequest.class));
    }

    @Test
    void shouldReturn404WhenBookingNotFoundForPayment() throws Exception {
        doThrow(new BookingNotFoundException(1L))
            .when(paymentService).processPayment(any(PaymentRequest.class));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Booking not found with ID: 1"));
    }

    @Test
    void shouldReturn400WhenPaymentCannotBeProcessedDueToStatus() throws Exception {
        doThrow(new PaymentProcessingException("CONFIRMED"))
            .when(paymentService).processPayment(any(PaymentRequest.class));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Logic Error"))
                .andExpect(jsonPath("$.message").value("Cannot process payment for booking with status: CONFIRMED"));
    }

    @Test
    void shouldHandleServerErrorDuringPaymentProcessing() throws Exception {
        doThrow(new RuntimeException("Payment gateway unavailable"))
            .when(paymentService).processPayment(any(PaymentRequest.class));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
