package com.lukianchykov.bookingsystem.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.domain.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Query("SELECT b FROM Booking b WHERE b.unit.id = :unitId AND b.status IN ('PENDING', 'CONFIRMED') AND " +
           "NOT (b.endDate < :startDate OR b.startDate > :endDate)")
    List<Booking> findConflictingBookings(
            @Param("unitId") Long unitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime dateTime);
}

