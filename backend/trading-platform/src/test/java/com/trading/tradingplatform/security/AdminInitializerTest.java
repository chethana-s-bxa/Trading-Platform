package com.trading.tradingplatform.security;

import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.Role;
import com.trading.tradingplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AdminInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final AdminInitializer initializer =
            new AdminInitializer(userRepository, encoder);

    @Test
    void init_adminAlreadyExists() {

        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        initializer.init();

        verify(userRepository, never()).save(any());
    }

    @Test
    void init_adminCreated() {

        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

        initializer.init();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("admin", savedUser.getUsername());
        assertEquals("admin@gmail.com", savedUser.getEmail());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals(0, savedUser.getBalance().intValue());

        // password should be encoded (not plain)
        assertNotEquals("admin@123", savedUser.getPassword());
    }


}