package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.controller.exception.UserAlreadyExistsException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.UserCreateRequest;
import com.lukianchykov.bookingsystem.dto.UserResponse;
import com.lukianchykov.bookingsystem.mapper.UserMapper;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final EventService eventService;

    private final UserMapper userMapper;

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .build();

        user = userRepository.save(user);

        eventService.createEvent("USER_CREATED", "User", user.getId(),
            "User created with email " + user.getEmail());

        return userMapper.toResponse(user);
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }
}