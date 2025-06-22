package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.controller.exception.UnitNotFoundException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.UnitCreateRequest;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import com.lukianchykov.bookingsystem.dto.UnitSearchRequest;
import com.lukianchykov.bookingsystem.dto.UnitUpdateRequest;
import com.lukianchykov.bookingsystem.mapper.UnitMapper;
import com.lukianchykov.bookingsystem.repository.UnitRepository;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UnitService {
    
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final UnitMapper unitMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public UnitResponse createUnit(UnitCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new UserNotFoundException(request.getOwnerId()));
        
        Unit unit = Unit.builder()
                .numberOfRooms(request.getNumberOfRooms())
                .accommodationType(request.getAccommodationType())
                .floor(request.getFloor())
                .baseCost(request.getBaseCost())
                .description(request.getDescription())
                .owner(owner)
                .build();

        unit = unitRepository.save(unit);

        eventService.createEvent("UNIT_CREATED", "Unit", unit.getId(),
            "Unit created with " + unit.getNumberOfRooms() + " rooms");

        publishAvailableUnitsChangedEvent("Unit created");
        
        return unitMapper.toResponse(unit);
    }

    public UnitResponse getUnit(Long id) {
        Unit unit = unitRepository.findById(id)
            .orElseThrow(() -> new UnitNotFoundException(id));
        return unitMapper.toResponse(unit);
    }

    public Page<UnitResponse> searchUnits(UnitSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        String accommodationType = request.getAccommodationType() != null ? 
                request.getAccommodationType().name() : null;
        
        Page<Unit> units = unitRepository.findAvailableUnits(
                request.getNumberOfRooms(),
                accommodationType,
                request.getFloor(),
                request.getMinCost(),
                request.getMaxCost(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
        
        return units.map(unitMapper::toResponse);
    }

    public UnitResponse updateUnit(Long id, UnitUpdateRequest request) {
        Unit unit = unitRepository.findById(id)
            .orElseThrow(() -> new UnitNotFoundException(id));

        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new UserNotFoundException(request.getOwnerId()));

        unit.setNumberOfRooms(request.getNumberOfRooms());
        unit.setAccommodationType(request.getAccommodationType());
        unit.setFloor(request.getFloor());
        unit.setBaseCost(request.getBaseCost());
        unit.setFinalCost(request.getFinalCost());
        unit.setDescription(request.getDescription());
        unit.setOwner(owner);

        unit = unitRepository.save(unit);

        eventService.createEvent("UNIT_UPDATED", "Unit", unit.getId(),
            "Unit updated");

        publishAvailableUnitsChangedEvent("Unit updated");

        return unitMapper.toResponse(unit);
    }

    public void deleteUnit(Long id) {
        Unit unit = unitRepository.findById(id)
            .orElseThrow(() -> new UnitNotFoundException(id));

        unitRepository.delete(unit);

        eventService.createEvent("UNIT_DELETED", "Unit", id,
            "Unit deleted");

        publishAvailableUnitsChangedEvent("Unit deleted");
    }

    public Long countAvailableUnitsFromDatabase() {
        log.debug("Querying database for available units count");
        return unitRepository.countAvailableUnits();
    }

    public Long countTotalUnits() {
        log.debug("Querying database for total units count");
        return unitRepository.count();
    }

    private void publishAvailableUnitsChangedEvent(String reason) {
        log.debug("Publishing available units changed event: {}", reason);
        eventPublisher.publishEvent(new AvailableUnitsChangedEvent(this));
    }
}
