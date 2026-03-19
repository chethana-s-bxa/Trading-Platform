package com.trading.tradingplatform.security;

import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.Role;
import com.trading.tradingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        boolean adminExists = userRepository.existsByRole(Role.ADMIN);

        if (adminExists) {
            return;
        }

        User admin = new User();

        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword(passwordEncoder.encode("admin@123"));
        admin.setBalance(BigDecimal.valueOf(0.0));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
    }
}