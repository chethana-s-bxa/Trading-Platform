package com.trading.tradingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.tradingplatform.exception.UserAlreadyExistsException;
import com.trading.tradingplatform.security.JwtService;
import com.trading.tradingplatform.service.UserService;
import com.trading.tradingplatform.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.trading.tradingplatform.dto.UserRegistrationRequest;
import com.trading.tradingplatform.dto.UserResponseDTO;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void registerUser_success() throws Exception {

        // ---------- Arrange ----------

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setUsername("testuser");
        response.setEmail("test@example.com");
        response.setBalance(BigDecimal.valueOf(10000.0));

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(response);


        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void registerUser_usernameAlreadyExists() throws Exception {

        // ---------- Arrange ----------

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock exception
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));


        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())   // 409
                .andExpect(jsonPath("$.message").value("Username already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void registerUser_emailAlreadyExists() throws Exception {

        // ---------- Arrange ----------

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock exception
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())   // 409
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }
}