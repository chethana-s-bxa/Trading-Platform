package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.LoginRequest;
import com.trading.tradingplatform.dto.LoginResponse;
import com.trading.tradingplatform.dto.UserRegistrationRequest;
import com.trading.tradingplatform.dto.UserResponseDTO;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.Role;
import com.trading.tradingplatform.exception.InvalidCredentialsException;
import com.trading.tradingplatform.exception.UserAlreadyExistsException;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_success() {

        // ---------- Arrange (Given) ----------

        // 1. Create input request
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // 2. Mock repository behavior
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // 3. Mock password encoding
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // 4. Mock save() behavior
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        // ---------- Act (When) ----------

        UserResponseDTO response = userService.registerUser(request);


        // ---------- Assert (Then) ----------

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        // Verify interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_usernameAlreadyExists() {

        // ---------- Arrange ----------

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock: username already exists
        when(userRepository.existsByUsername("testuser")).thenReturn(true);


        // ---------- Act & Assert ----------

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(request);
        });

        // Verify that email check and save are NEVER called
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_emailAlreadyExists() {

        // ---------- Arrange ----------

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Username is fine
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        // Email already exists
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);


        // ---------- Act & Assert ----------

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(request);
        });

        // Verify save is NOT called
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUser_emailNotFound() {

        // ---------- Arrange ----------

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock: user not found
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());


        // ---------- Act & Assert ----------

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.loginUser(request);
        });

        // Verify password check and token generation NEVER happen
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void loginUser_invalidPassword() {

        // ---------- Arrange ----------

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        // Mock user from DB
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Password does NOT match
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);


        // ---------- Act & Assert ----------

        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(request);
        });

        // Verify token is NEVER generated
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void loginUser_success() {

        // ---------- Arrange ----------

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock user from DB
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setBalance(BigDecimal.valueOf(10000.0));
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Password matches
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        // Mock JWT token
        when(jwtService.generateToken("test@example.com", Role.USER))
                .thenReturn("mocked-jwt-token");


        // ---------- Act ----------

        LoginResponse response = userService.loginUser(request);


        // ---------- Assert ----------

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());

        // Verify interactions
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken("test@example.com", Role.USER);
    }
}
