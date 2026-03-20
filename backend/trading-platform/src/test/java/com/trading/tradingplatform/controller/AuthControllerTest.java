package com.trading.tradingplatform.controller;


import com.trading.tradingplatform.dto.LoginRequest;
import com.trading.tradingplatform.dto.LoginResponse;
import com.trading.tradingplatform.exception.InvalidCredentialsException;
import com.trading.tradingplatform.security.JwtService;
import com.trading.tradingplatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void loginUser_success() throws Exception {

        // ---------- Arrange ----------

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        LoginResponse response = LoginResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .balance(BigDecimal.valueOf(10000.0))
                .token("mocked-token")
                .message("Login successful")
                .build();

        when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(response);


        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("mocked-token"));
    }

    @Test
    void loginUser_invalidCredentials() throws Exception {

        // ---------- Arrange ----------

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        // Mock service to throw exception
        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid login credentials"));


        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isUnauthorized())   // 401
                .andExpect(jsonPath("$.message").value("Invalid login credentials"))
                .andExpect(jsonPath("$.status").value(401));
    }
}
