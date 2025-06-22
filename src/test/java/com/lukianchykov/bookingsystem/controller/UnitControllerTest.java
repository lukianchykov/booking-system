package com.lukianchykov.bookingsystem.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukianchykov.bookingsystem.controller.exception.UnitNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
import com.lukianchykov.bookingsystem.controller.handler.GlobalExceptionHandler;
import com.lukianchykov.bookingsystem.domain.AccommodationType;
import com.lukianchykov.bookingsystem.dto.UnitCreateRequest;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import com.lukianchykov.bookingsystem.dto.UnitSearchRequest;
import com.lukianchykov.bookingsystem.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnitController.class)
@Import(GlobalExceptionHandler.class)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnitService unitService;

    @Autowired
    private ObjectMapper objectMapper;

    private UnitResponse unitResponse;
    private UnitCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        unitResponse = UnitResponse.builder()
            .id(1L)
            .numberOfRooms(3)
            .accommodationType(AccommodationType.FLAT)
            .floor(2)
            .baseCost(new BigDecimal("100.00"))
            .finalCost(new BigDecimal("115.00"))
            .description("Test unit")
            .ownerName("Test Owner")
            .createdAt(LocalDateTime.now())
            .build();

        createRequest = new UnitCreateRequest();
        createRequest.setNumberOfRooms(3);
        createRequest.setAccommodationType(AccommodationType.FLAT);
        createRequest.setFloor(2);
        createRequest.setBaseCost(new BigDecimal("100.00"));
        createRequest.setDescription("Test unit");
        createRequest.setOwnerId(1L);
    }

    @Test
    void shouldCreateUnitSuccessfully() throws Exception {
        when(unitService.createUnit(any(UnitCreateRequest.class))).thenReturn(unitResponse);

        mockMvc.perform(post("/api/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.number_of_rooms").value(3))
            .andExpect(jsonPath("$.accommodation_type").value("FLAT"))
            .andExpect(jsonPath("$.final_cost").value(115.00))
            .andExpect(jsonPath("$.owner_name").value("Test Owner"));
    }

    @Test
    void shouldReturn404WhenOwnerNotFoundDuringCreation() throws Exception {
        when(unitService.createUnit(any(UnitCreateRequest.class)))
            .thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(post("/api/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with ID: 1"))
            .andExpect(jsonPath("$.path").value("/api/units"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGetUnitByIdSuccessfully() throws Exception {
        when(unitService.getUnit(1L)).thenReturn(unitResponse);

        mockMvc.perform(get("/api/units/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.number_of_rooms").value(3))
            .andExpect(jsonPath("$.accommodation_type").value("FLAT"));
    }

    @Test
    void shouldReturn404WhenUnitNotFoundById() throws Exception {
        when(unitService.getUnit(1L)).thenThrow(new UnitNotFoundException(1L));

        mockMvc.perform(get("/api/units/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("Unit not found with ID: 1"))
            .andExpect(jsonPath("$.path").value("/api/units/1"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldSearchUnitsWithFilters() throws Exception {
        Page<UnitResponse> unitPage = new PageImpl<>(List.of(unitResponse));
        when(unitService.searchUnits(any(UnitSearchRequest.class))).thenReturn(unitPage);

        mockMvc.perform(get("/api/units/search")
                .param("numberOfRooms", "3")
                .param("accommodationType", "FLAT")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(3).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldHandleServerErrorDuringUnitSearch() throws Exception {
        when(unitService.searchUnits(any(UnitSearchRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/units/search")
                .param("numberOfRooms", "3"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}