package com.lukianchykov.bookingsystem.service;

import java.time.LocalDateTime;
import java.util.List;

import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;
import com.lukianchykov.bookingsystem.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingExpirationService {

    private final BookingRepository bookingRepository;

    private final CacheService cacheService;

    @Scheduled(fixedRate = 60000)
    public void expireBookings() {
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
            BookingStatus.PENDING, LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            log.info("Expired booking with id: {}", booking.getId());
        }

        if (!expiredBookings.isEmpty()) {
            cacheService.evictAvailableUnitsCache();
        }
    }
}