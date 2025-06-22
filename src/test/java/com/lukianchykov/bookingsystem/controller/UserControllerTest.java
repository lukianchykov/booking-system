package com.lukianchykov.bookingsystem.controller;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukianchykov.bookingsystem.controller.exception.UserAlreadyExistsException;
import com.lukianchykov.bookingsystem.controller.exception.UserNotFoundException;
import com.lukianchykov.bookingsystem.controller.handler.GlobalExceptionHandler;
import com.lukianchykov.bookingsystem.dto.UserCreateRequest;
import com.lukianchykov.bookingsystem.dto.UserResponse;
import com.lukianchykov.bookingsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
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
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class)))
            .thenThrow(new UserAlreadyExistsException("test@example.com"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("User Already Exists"))
            .andExpect(jsonPath("$.message").value("User with email already exists: test@example.com"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        when(userService.getUser(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        when(userService.getUser(1L)).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with ID: 1"))
            .andExpect(jsonPath("$.path").value("/api/users/1"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleServerErrorDuringUserRetrieval() throws Exception {
        when(userService.getUser(1L))
            .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}