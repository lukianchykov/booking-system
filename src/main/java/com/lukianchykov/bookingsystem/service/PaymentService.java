package com.lukianchykov.bookingsystem.service;

import java.util.UUID;

import com.lukianchykov.bookingsystem.controller.exception.BookingNotFoundException;
import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.domain.Payment;
import com.lukianchykov.bookingsystem.dto.PaymentRequest;
import com.lukianchykov.bookingsystem.repository.BookingRepository;
import com.lukianchykov.bookingsystem.repository.PaymentRepository;
import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final BookingRepository bookingRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final EventService eventService;

    public void processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Cannot process payment for booking with status: " + booking.getStatus());
        }

        Payment payment = Payment.builder()
            .booking(booking)
            .amount(booking.getTotalCost())
            .paymentMethod(request.getPaymentMethod())
            .transactionId(UUID.randomUUID().toString())
            .build();

        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        eventService.createEvent("PAYMENT_PROCESSED", "Payment", payment.getId(),
            "Payment processed for booking " + booking.getId());

        publishAvailableUnitsChangedEvent();
    }

    private void publishAvailableUnitsChangedEvent() {
        log.debug("Publishing available units changed event: {}", "Payment created");
        eventPublisher.publishEvent(new AvailableUnitsChangedEvent(this));
    }
}