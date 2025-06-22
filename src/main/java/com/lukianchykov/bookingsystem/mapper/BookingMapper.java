package com.lukianchykov.bookingsystem.mapper;

import com.lukianchykov.bookingsystem.domain.Booking;
import com.lukianchykov.bookingsystem.dto.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "totalCost", source = "totalCost")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    BookingResponse toResponse(Booking booking);
}