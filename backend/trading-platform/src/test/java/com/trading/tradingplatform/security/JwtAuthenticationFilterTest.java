package com.trading.tradingplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_skipPublicEndpoints() throws Exception {

        when(request.getServletPath()).thenReturn("/api/v1/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_noAuthHeader() throws Exception {

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidHeader() throws Exception {

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {

        String token = "valid-token";
        String username = "test@gmail.com";

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.isTokenValid(token, username)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_noAuthentication() throws Exception {

        String token = "invalid-token";

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn("test@gmail.com");
        when(jwtService.isTokenValid(token, "test@gmail.com")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_usernameNull() throws Exception {

        String token = "token";

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_authAlreadyExists() throws Exception {

        String token = "token";

        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn("test@gmail.com");

        // Set existing authentication
        SecurityContextHolder.getContext()
                .setAuthentication(mock(org.springframework.security.core.Authentication.class));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipRegisterEndpoint() throws Exception {

        when(request.getServletPath()).thenReturn("/api/v1/users/register");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }




}