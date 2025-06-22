package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.lukianchykov.bookingsystem.controller.exception.BookingNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.InvalidBookingStatusException;
import com.lukianchykov.bookingsystem.controller.exception.UnitNotAvailableException;
import com.lukianchykov.bookingsystem.controller.exception.UnitNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;

    private final UnitRepository unitRepository;

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final EventService eventService;

    private final BookingMapper bookingMapper;

    public BookingResponse createBooking(BookingCreateRequest request) {
        Unit unit = unitRepository.findById(request.getUnitId())
            .orElseThrow(() -> new UnitNotFoundException(request.getUnitId()));

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            request.getUnitId(), request.getStartDate(), request.getEndDate());

        if (!conflicts.isEmpty()) {
            throw new UnitNotAvailableException();
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal totalCost = unit.getFinalCost().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
            .unit(unit)
            .user(user)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalCost(totalCost)
            .status(BookingStatus.PENDING)
            .build();

        booking = bookingRepository.save(booking);

        eventService.createEvent("BOOKING_CREATED", "Booking", booking.getId(),
            "Booking created for unit " + unit.getId());

        eventPublisher.publishEvent(new AvailableUnitsChangedEvent(this));

        return bookingMapper.toResponse(booking);
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException(booking.getStatus().toString(), "cancel");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        eventService.createEvent("BOOKING_CANCELLED", "Booking", booking.getId(),
            "Booking cancelled");

        eventPublisher.publishEvent(new AvailableUnitsChangedEvent(this));

        return bookingMapper.toResponse(booking);
    }

    public BookingResponse getBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        return bookingMapper.toResponse(booking);
    }
}