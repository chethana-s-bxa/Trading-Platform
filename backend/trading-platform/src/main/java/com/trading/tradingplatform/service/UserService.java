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
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    /**
     * Registers a new user in the system
     * after validating username and email uniqueness.
     *
     * @param request DTO containing username, email and password
     * @return UserResponseDTO containing user details excluding password
     */
    public UserResponseDTO registerUser(UserRegistrationRequest request){
        logger.info("Attempting to register user with email: {}", request.getEmail());

        if(userRepository.existsByUsername(request.getUsername())){
            logger.warn("Registration failed - Username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            logger.warn("Registration failed - Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }
        User user = createUserFromRequest(request);

        logger.info("User registration successful for username: {}", user.getUsername());

        return convertUserToUserResponse(user);
    }

    /**
     * Maps the UserRegistrationRequest DTO to a
     * User entity and persists it in the database.
     * @param request DTO containing username, email and password
     * @return the saved User entity
     */
    private User createUserFromRequest(UserRegistrationRequest request){

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(BigDecimal.valueOf(10000.0));   // if you are giving default balance

        user.setRole(Role.USER);    // IMPORTANT

        return userRepository.save(user);
    }

    /**
     * DTO returned to the client after successful registration
     * @param user main entity
     * @return UserResponseDTO user details excluding password
     */
    public UserResponseDTO convertUserToUserResponse(User user){
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBalance(user.getBalance());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    /**
     * Authenticates a user using the provided email and password.
     *
     * @param request LoginRequest DTO containing the user's email and password
     * @return LoginResponse DTO containing user details and login status message
     * @throws RuntimeException if no user is found with the provided email
     */
    public LoginResponse loginUser(LoginRequest request){

        logger.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - Email not found: {}", request.getEmail());
                    return new UserAlreadyExistsException("Email not found");
                });

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            logger.warn("Login failed - Invalid credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid login credentials");
        }

        Role role = user.getRole() != null ? user.getRole(): Role.USER;

        String token = jwtService.generateToken(user.getEmail(), role);

        logger.info("Login successful for user ID: {}", user.getId());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(user.getBalance())
                .token(token)
                .message("Login successful")
                .build();
    }
}
