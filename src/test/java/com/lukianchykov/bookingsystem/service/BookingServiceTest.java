package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.lukianchykov.bookingsystem.domain.AccommodationType;
import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.BookingCreateRequest;
import com.lukianchykov.bookingsystem.dto.BookingResponse;
import com.lukianchykov.bookingsystem.mapper.BookingMapper;
import com.lukianchykov.bookingsystem.repository.BookingRepository;
import com.lukianchykov.bookingsystem.repository.UnitRepository;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EventService eventService;


    @InjectMocks
    private BookingService bookingService;

    private Unit testUnit;

    private User testUser;

    private Booking testBooking;

    private BookingResponse testBookingResponse;

    private BookingCreateRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .createdAt(LocalDateTime.now())
            .build();

        testUnit = Unit.builder()
            .id(1L)
            .numberOfRooms(2)
            .accommodationType(AccommodationType.APARTMENTS)
            .floor(3)
            .baseCost(BigDecimal.valueOf(100))
            .finalCost(BigDecimal.valueOf(100))
            .description("Test unit")
            .owner(testUser)
            .createdAt(LocalDateTime.now())
            .build();

        testBooking = Booking.builder()
            .id(1L)
            .unit(testUnit)
            .user(testUser)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(3))
            .status(BookingStatus.PENDING)
            .totalCost(BigDecimal.valueOf(200))
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();

        testBookingResponse = BookingResponse.builder()
            .id(1L)
            .unitId(1L)
            .userId(1L)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(3))
            .totalCost(BigDecimal.valueOf(200))
            .status(BookingStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();

        testRequest = new BookingCreateRequest();
        testRequest.setUnitId(1L);
        testRequest.setUserId(1L);
        testRequest.setStartDate(LocalDate.now().plusDays(1));
        testRequest.setEndDate(LocalDate.now().plusDays(3));
    }

    @Test
    void createBooking_Success() {

        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.findConflictingBookings(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponse result = bookingService.createBooking(testRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUnitId());
        assertEquals(1L, result.getUserId());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(AvailableUnitsChangedEvent.class));
    }

    @Test
    void createBooking_UnitNotFound_ThrowsException() {

        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.createBooking(testRequest));
        assertEquals("Unit not found with ID: 1", exception.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createBooking_UserNotFound_ThrowsException() {

        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.createBooking(testRequest));
        assertEquals("User not found with ID: 1", exception.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createBooking_ConflictingBookings_ThrowsException() {

        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.findConflictingBookings(any(), any(), any()))
            .thenReturn(List.of(testBooking));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.createBooking(testRequest));
        assertEquals("Unit is not available for the selected dates", exception.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void cancelBooking_Success_PendingStatus() {

        testBooking.setStatus(BookingStatus.PENDING);
        testBookingResponse.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponse result = bookingService.cancelBooking(1L);

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());

        verify(bookingRepository).save(testBooking);
        verify(eventPublisher).publishEvent(any(AvailableUnitsChangedEvent.class));
    }

    @Test
    void cancelBooking_Success_ConfirmedStatus() {

        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBookingResponse.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponse result = bookingService.cancelBooking(1L);

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());

        verify(bookingRepository).save(testBooking);
        verify(eventPublisher).publishEvent(any(AvailableUnitsChangedEvent.class));
    }

    @Test
    void cancelBooking_BookingNotFound_ThrowsException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.cancelBooking(1L));
        assertEquals("Booking not found with ID: 1", exception.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void cancelBooking_InvalidStatus_ThrowsException() {

        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.cancelBooking(1L));
        assertEquals("Cannot cancel booking with status: CANCELLED", exception.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getBooking_Success() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);

        BookingResponse result = bookingService.getBooking(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUnitId());
        assertEquals(1L, result.getUserId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void getBooking_NotFound_ThrowsException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> bookingService.getBooking(1L));
        assertEquals("Booking not found", exception.getMessage());
    }
}