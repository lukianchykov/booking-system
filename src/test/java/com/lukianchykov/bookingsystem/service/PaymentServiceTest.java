package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.domain.Payment;
import com.lukianchykov.bookingsystem.dto.PaymentRequest;
import com.lukianchykov.bookingsystem.repository.BookingRepository;
import com.lukianchykov.bookingsystem.repository.PaymentRepository;
import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EventService eventService;

    @InjectMocks
    private PaymentService paymentService;

    private Booking mockBooking;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        mockBooking = Booking.builder()
                .id(1L)
                .status(BookingStatus.PENDING)
                .totalCost(new BigDecimal("100.00"))
                .build();

        paymentRequest = PaymentRequest.builder()
                .bookingId(1L)
                .paymentMethod("CREDIT_CARD")
                .build();
    }

    @Test
    void processPayment_ShouldSucceed_WhenValidPendingBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mock(Payment.class));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        paymentService.processPayment(paymentRequest);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(mockBooking, savedPayment.getBooking());
        assertEquals(new BigDecimal("100.00"), savedPayment.getAmount());
        assertEquals("CREDIT_CARD", savedPayment.getPaymentMethod());
        assertNotNull(savedPayment.getTransactionId());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        
        Booking savedBooking = bookingCaptor.getValue();
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());

        verify(eventService).createEvent(
                eq("PAYMENT_PROCESSED"),
                eq("Payment"),
                any(),
                eq("Payment processed for booking 1")
        );

        ArgumentCaptor<AvailableUnitsChangedEvent> eventCaptor = ArgumentCaptor.forClass(AvailableUnitsChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        AvailableUnitsChangedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(paymentService, publishedEvent.getSource());
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> paymentService.processPayment(paymentRequest));
        
        assertEquals("Booking not found", exception.getMessage());
        
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventService, never()).createEvent(anyString(), anyString(), any(), anyString());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingStatusIsNotPending() {
        mockBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> paymentService.processPayment(paymentRequest));
        
        assertEquals("Cannot process payment for booking with status: CONFIRMED", exception.getMessage());
        
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventService, never()).createEvent(anyString(), anyString(), any(), anyString());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingStatusIsCancelled() {
        mockBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> paymentService.processPayment(paymentRequest));
        
        assertEquals("Cannot process payment for booking with status: CANCELLED", exception.getMessage());
    }
}