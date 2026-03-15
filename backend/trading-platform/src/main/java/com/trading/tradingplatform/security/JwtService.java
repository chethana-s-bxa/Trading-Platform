package com.trading.tradingplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Secret key used to sign and verify the JWT token
    @Value("${jwt.secret}")
    private String secret;

    // Token expiration time in milliseconds
    @Value("${jwt.expiration}")
    private long expiration;


    /**
     * Generates the signing key used for creating and verifying JWT tokens.
     *
     * @return cryptographic signing key
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a JWT token for an authenticated user.
     * The token contains the username as subject along with
     * issued time and expiration time.
     *
     * @param username the username or email of the authenticated user
     * @return generated JWT token
     */
    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return username stored in the token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts all claims (payload information) from the JWT token.
     * The claims include subject, issued time, and expiration time.
     *
     * @param token the JWT token
     * @return claims contained inside the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks whether the JWT token has expired.
     *
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Validates the JWT token.
     * A token is valid if:
     * 1. Username inside the token matches the expected username
     * 2. Token is not expired
     *
     * @param token the JWT token
     * @param username expected username
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, String username) {

        String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username) && !isTokenExpired(token);
    }
}