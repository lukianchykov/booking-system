package com.lukianchykov.bookingsystem.mapper;

import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "createdAt", source = "createdAt")
    UserResponse toResponse(User user);
}