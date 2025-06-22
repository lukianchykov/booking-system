package com.lukianchykov.bookingsystem.mapper;

import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.dto.UnitResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "numberOfRooms", source = "numberOfRooms")
    @Mapping(target = "accommodationType", source = "accommodationType")
    @Mapping(target = "floor", source = "floor")
    @Mapping(target = "baseCost", source = "baseCost")
    @Mapping(target = "finalCost", source = "finalCost")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "available", constant = "true")
    UnitResponse toResponse(Unit unit);
}