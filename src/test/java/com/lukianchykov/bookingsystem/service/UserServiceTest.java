package com.lukianchykov.bookingsystem.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.dto.UserCreateRequest;
import com.lukianchykov.bookingsystem.dto.UserResponse;
import com.lukianchykov.bookingsystem.mapper.UserMapper;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    private UserResponse userResponse;

    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .createdAt(LocalDateTime.now())
            .build();

        userResponse = UserResponse.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .createdAt(LocalDateTime.now())
            .build();

        createRequest = new UserCreateRequest();
        createRequest.setEmail("test@example.com");
        createRequest.setName("Test User");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse response = userService.createUser(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse response = userService.getUser(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }
}