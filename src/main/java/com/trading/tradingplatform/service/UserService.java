package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.LoginRequest;
import com.trading.tradingplatform.dto.LoginResponse;
import com.trading.tradingplatform.dto.UserRegistrationRequest;
import com.trading.tradingplatform.dto.UserResponseDTO;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user in the system
     * after validating username and email uniqueness.
     *
     * @param request DTO containing username, email and password
     * @return UserResponseDTO containing user details excluding password
     */
    public UserResponseDTO registerUser(UserRegistrationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        return convertUserToUserResponse(createUserFromRequest(request));
    }

    /**
     * Maps the UserRegistrationRequest DTO to a
     * User entity and persists it in the database.
     * @param request DTO containing username, email and password
     * @return the saved User entity
     */
    public User createUserFromRequest(UserRegistrationRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(1000.0);

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
        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(()-> new RuntimeException("Email not found"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid login credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

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
