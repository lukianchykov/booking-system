package com.lukianchykov.bookingsystem.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lukianchykov.bookingsystem.domain.Unit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    
    @Query("SELECT u FROM Unit u WHERE " +
           "(:numberOfRooms IS NULL OR u.numberOfRooms = :numberOfRooms) AND " +
           "(:accommodationType IS NULL OR u.accommodationType = :accommodationType) AND " +
           "(:floor IS NULL OR u.floor = :floor) AND " +
           "(:minCost IS NULL OR u.finalCost >= :minCost) AND " +
           "(:maxCost IS NULL OR u.finalCost <= :maxCost) AND " +
           "(:startDate IS NULL OR :endDate IS NULL OR u.id NOT IN " +
           "(SELECT b.unit.id FROM Booking b WHERE b.status IN ('PENDING', 'CONFIRMED') AND " +
           "NOT (b.endDate < :startDate OR b.startDate > :endDate)))")
    Page<Unit> findAvailableUnits(
            @Param("numberOfRooms") Integer numberOfRooms,
            @Param("accommodationType") String accommodationType,
            @Param("floor") Integer floor,
            @Param("minCost") BigDecimal minCost,
            @Param("maxCost") BigDecimal maxCost,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(DISTINCT u.id) FROM Unit u WHERE " +
           "u.id NOT IN (SELECT b.unit.id FROM Booking b WHERE b.status IN ('PENDING', 'CONFIRMED'))")
    Long countAvailableUnits();
}