package com.trading.tradingplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter responsible for validating JWT tokens
 * for incoming HTTP requests.
 * If the token is valid, the user is authenticated and allowed
 * to access secured endpoints.
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service responsible for handling JWT operations such as
     * generating tokens, extracting username, and validating tokens.
     */
    private final JwtService jwtService;

    /**
     * Core filter method that executes once per request.
     *
     * This method performs JWT authentication by:
     *
     *     - Reading the Authorization header
     *     - Checking if it contains a Bearer token
     *     - Extracting the JWT token
     *     - Extracting username from the token
     *     - Validating the token
     *     - Setting authentication in SecurityContextHolder
     *
     * If the token is valid, the request is considered authenticated
     * and allowed to proceed to the next filter or controller.
     *
     * @param request incoming HTTP request
     * @param response HTTP response
     * @param filterChain chain of filters that the request passes through
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // If Authorization header is missing or does not contain Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from header
        String token = authHeader.substring(7);

        // Extract username from JWT
        String username = jwtService.extractUsername(token);

        // Authenticate user if username exists and user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate the JWT token
            if (jwtService.isTokenValid(token, username)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);

                // Set authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}