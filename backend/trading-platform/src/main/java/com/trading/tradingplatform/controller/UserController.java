package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.UserRegistrationRequest;
import com.trading.tradingplatform.dto.UserResponseDTO;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRegistrationRequest request){
        return ResponseEntity.ok(userService.registerUser(request));
    }
}
