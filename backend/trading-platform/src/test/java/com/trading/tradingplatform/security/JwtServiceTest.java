package com.trading.tradingplatform.security;

import com.trading.tradingplatform.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {

        jwtService = new JwtService();

        // Inject secret
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "mysecretkeymysecretkeymysecretkey12"); // 32+ chars

        // Inject expiration
        Field expField = JwtService.class.getDeclaredField("expiration");
        expField.setAccessible(true);
        expField.set(jwtService, 1000 * 60 * 10); // 10 mins
    }

    @Test
    void generateToken_and_extractUsername() {

        String token = jwtService.generateToken("test@gmail.com", Role.USER);

        String username = jwtService.extractUsername(token);

        assertEquals("test@gmail.com", username);
    }

    @Test
    void extractRole_success() {

        String token = jwtService.generateToken("test@gmail.com", Role.ADMIN);

        String role = jwtService.extractRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenValid_true() {

        String token = jwtService.generateToken("test@gmail.com", Role.USER);

        boolean valid = jwtService.isTokenValid(token, "test@gmail.com");

        assertTrue(valid);
    }

    @Test
    void isTokenValid_wrongUsername() {

        String token = jwtService.generateToken("test@gmail.com", Role.USER);

        boolean valid = jwtService.isTokenValid(token, "wrong@gmail.com");

        assertFalse(valid);
    }

    @Test
    void isTokenValid_expiredToken() throws Exception {

        // Set very short expiry
        Field expField = JwtService.class.getDeclaredField("expiration");
        expField.setAccessible(true);
        expField.set(jwtService, 1); // 1 ms

        String token = jwtService.generateToken("test@gmail.com", Role.USER);

        Thread.sleep(5);

        assertThrows(Exception.class, () ->
                jwtService.isTokenValid(token, "test@gmail.com")
        );
    }


}