package com.lukianchykov.bookingsystem.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukianchykov.bookingsystem.controller.exception.BookingNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.InvalidBookingStatusException;
import com.lukianchykov.bookingsystem.controller.exception.UnitNotAvailableException;
import com.lukianchykov.bookingsystem.controller.exception.UnitNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
import com.lukianchykov.bookingsystem.controller.handler.GlobalExceptionHandler;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.dto.BookingCreateRequest;
import com.lukianchykov.bookingsystem.dto.BookingResponse;
import com.lukianchykov.bookingsystem.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingResponse bookingResponse;
    private BookingCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        bookingResponse = BookingResponse.builder()
            .id(1L)
            .unitId(1L)
            .userId(1L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(3))
            .totalCost(new BigDecimal("345.00"))
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        createRequest = new BookingCreateRequest();
        createRequest.setUnitId(1L);
        createRequest.setUserId(1L);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusDays(3));
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(bookingResponse);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.unit_id").value(1L))
            .andExpect(jsonPath("$.user_id").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.total_cost").value(345.00));
    }

    @Test
    void shouldReturn404WhenUnitNotFoundDuringCreation() throws Exception {
        when(bookingService.createBooking(any(BookingCreateRequest.class)))
            .thenThrow(new UnitNotFoundException(1L));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("Unit not found with ID: 1"));
    }

    @Test
    void shouldReturn404WhenUserNotFoundDuringCreation() throws Exception {
        when(bookingService.createBooking(any(BookingCreateRequest.class)))
            .thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with ID: 1"));
    }

    @Test
    void shouldReturn409WhenUnitNotAvailable() throws Exception {
        when(bookingService.createBooking(any(BookingCreateRequest.class)))
            .thenThrow(new UnitNotAvailableException());

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Unit Not Available"))
            .andExpect(jsonPath("$.message").value("Unit is not available for the selected dates"));
    }

    @Test
    void shouldGetBookingByIdSuccessfully() throws Exception {
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.total_cost").value(345.00));
    }

    @Test
    void shouldReturn404WhenBookingNotFoundById() throws Exception {
        when(bookingService.getBooking(1L)).thenThrow(new BookingNotFoundException(1L));

        mockMvc.perform(get("/api/bookings/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("Booking not found with ID: 1"));
    }

    @Test
    void shouldCancelBookingSuccessfully() throws Exception {
        BookingResponse cancelledResponse = BookingResponse.builder()
            .id(1L)
            .unitId(1L)
            .userId(1L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(3))
            .totalCost(new BigDecimal("345.00"))
            .status(BookingStatus.CANCELLED)
            .createdAt(LocalDateTime.now())
            .build();

        when(bookingService.cancelBooking(1L)).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturn404WhenCancellingNonExistentBooking() throws Exception {
        when(bookingService.cancelBooking(1L)).thenThrow(new BookingNotFoundException(1L));

        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("Booking not found with ID: 1"));
    }

    @Test
    void shouldReturn400WhenCancellingBookingWithInvalidStatus() throws Exception {
        when(bookingService.cancelBooking(1L))
            .thenThrow(new InvalidBookingStatusException("COMPLETED", "cancel"));

        mockMvc.perform(put("/api/bookings/1/cancel"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Business Logic Error"))
            .andExpect(jsonPath("$.message").value("Cannot cancel booking with status: COMPLETED"));
    }
}
