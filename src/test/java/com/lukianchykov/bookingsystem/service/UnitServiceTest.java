package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import com.lukianchykov.bookingsystem.domain.AccommodationType;
import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.UnitCreateRequest;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import com.lukianchykov.bookingsystem.dto.UnitSearchRequest;
import com.lukianchykov.bookingsystem.mapper.UnitMapper;
import com.lukianchykov.bookingsystem.repository.UnitRepository;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private UnitMapper unitMapper;

    @InjectMocks
    private UnitService unitService;

    private User testOwner;

    private Unit testUnit;

    private UnitResponse testUnitResponse;

    private UnitCreateRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testOwner = User.builder()
            .id(1L)
            .email("owner@example.com")
            .name("Test Owner")
            .createdAt(LocalDateTime.now())
            .build();

        testUnit = Unit.builder()
            .id(1L)
            .numberOfRooms(2)
            .accommodationType(AccommodationType.APARTMENTS)
            .floor(3)
            .baseCost(BigDecimal.valueOf(100))
            .description("Test unit")
            .owner(testOwner)
            .createdAt(LocalDateTime.now())
            .build();

        testUnitResponse = UnitResponse.builder()
            .id(1L)
            .numberOfRooms(2)
            .accommodationType(AccommodationType.APARTMENTS)
            .floor(3)
            .baseCost(BigDecimal.valueOf(100.00))
            .finalCost(BigDecimal.valueOf(120.00))
            .description("Test unit")
            .ownerName("Test Owner")
            .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .available(true)
            .build();

        testCreateRequest = new UnitCreateRequest();
        testCreateRequest.setOwnerId(1L);
        testCreateRequest.setNumberOfRooms(2);
        testCreateRequest.setAccommodationType(AccommodationType.APARTMENTS);
        testCreateRequest.setFloor(3);
        testCreateRequest.setBaseCost(BigDecimal.valueOf(100));
        testCreateRequest.setDescription("Test unit");
    }

    @Test
    void createUnit_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(unitRepository.save(any(Unit.class))).thenReturn(testUnit);
        when(unitMapper.toResponse(any(Unit.class))).thenReturn(testUnitResponse);
        doNothing().when(cacheService).evictAvailableUnitsCache();

        UnitResponse result = unitService.createUnit(testCreateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getNumberOfRooms());
        assertEquals(AccommodationType.APARTMENTS, result.getAccommodationType());
        assertEquals(3, result.getFloor());
        assertEquals(BigDecimal.valueOf(100.0), result.getBaseCost());
        assertEquals("Test unit", result.getDescription());
        assertEquals("Test Owner", result.getOwnerName());
        assertTrue(result.getAvailable());

        verify(unitRepository).save(any(Unit.class));
        verify(cacheService).evictAvailableUnitsCache();
    }

    @Test
    void createUnit_OwnerNotFound_ThrowsException() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> unitService.createUnit(testCreateRequest));
        assertEquals("Owner not found", exception.getMessage());

        verify(unitRepository, never()).save(any());
        verify(cacheService, never()).evictAvailableUnitsCache();
    }

    @Test
    void searchUnits_Success() {

        UnitSearchRequest searchRequest = new UnitSearchRequest();
        searchRequest.setNumberOfRooms(2);
        searchRequest.setAccommodationType(AccommodationType.APARTMENTS);
        searchRequest.setFloor(3);
        searchRequest.setMinCost(BigDecimal.valueOf(50));
        searchRequest.setMaxCost(BigDecimal.valueOf(150));
        searchRequest.setStartDate(LocalDate.now().plusDays(1));
        searchRequest.setEndDate(LocalDate.now().plusDays(3));
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("baseCost");
        searchRequest.setSortDirection("ASC");

        Page<Unit> mockPage = new PageImpl<>(Collections.singletonList(testUnit));
        when(unitRepository.findAvailableUnits(
            eq(2), eq("APARTMENTS"), eq(3),
            eq(BigDecimal.valueOf(50)), eq(BigDecimal.valueOf(150)),
            any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(unitMapper.toResponse(any(Unit.class))).thenReturn(testUnitResponse);

        Page<UnitResponse> result = unitService.searchUnits(searchRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        UnitResponse unitResponse = result.getContent().getFirst();
        assertEquals(1L, unitResponse.getId());
        assertEquals(2, unitResponse.getNumberOfRooms());
        assertEquals(AccommodationType.APARTMENTS, unitResponse.getAccommodationType());
    }

    @Test
    void searchUnits_WithNullAccommodationType() {

        UnitSearchRequest searchRequest = new UnitSearchRequest();
        searchRequest.setAccommodationType(null);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("baseCost");
        searchRequest.setSortDirection("ASC");

        Page<Unit> mockPage = new PageImpl<>(Collections.singletonList(testUnit));
        when(unitRepository.findAvailableUnits(
            any(), isNull(), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);

        when(unitMapper.toResponse(any(Unit.class))).thenReturn(testUnitResponse);

        Page<UnitResponse> result = unitService.searchUnits(searchRequest);

        assertNotNull(result);
        verify(unitRepository).findAvailableUnits(
            any(), isNull(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void searchUnits_WithDescendingSort() {

        UnitSearchRequest searchRequest = new UnitSearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("baseCost");
        searchRequest.setSortDirection("DESC");

        Page<Unit> mockPage = new PageImpl<>(Collections.singletonList(testUnit));

        when(unitRepository.findAvailableUnits(
            any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);

        when(unitMapper.toResponse(any(Unit.class))).thenReturn(testUnitResponse);

        unitService.searchUnits(searchRequest);

        verify(unitRepository).findAvailableUnits(
            any(), any(), any(), any(), any(), any(), any(),
            argThat(pageable -> Objects.requireNonNull(pageable.getSort().getOrderFor("baseCost")).getDirection() == Sort.Direction.DESC));
    }

    @Test
    void getUnit_Success() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(unitMapper.toResponse(any(Unit.class))).thenReturn(testUnitResponse);

        UnitResponse result = unitService.getUnit(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getNumberOfRooms());
        assertEquals(AccommodationType.APARTMENTS, result.getAccommodationType());
        assertEquals(3, result.getFloor());
        assertEquals(BigDecimal.valueOf(100.0), result.getBaseCost());
        assertEquals("Test unit", result.getDescription());
        assertEquals("Test Owner", result.getOwnerName());
        assertTrue(result.getAvailable());
    }

    @Test
    void getUnit_NotFound_ThrowsException() {
        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> unitService.getUnit(1L));
        assertEquals("Unit not found", exception.getMessage());
    }

    @Test
    void countAvailableUnitsFromDatabase_Success() {
        when(unitRepository.countAvailableUnits()).thenReturn(5L);

        Long result = unitService.countAvailableUnitsFromDatabase();

        assertEquals(5L, result);
        verify(unitRepository).countAvailableUnits();
    }
}