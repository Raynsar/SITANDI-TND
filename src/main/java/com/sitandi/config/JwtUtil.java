package com.sitandi.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT Utility
 */
@Component
public class JwtUtil {

    private static final String CLAIM_ROLE = "role";

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationMs) {

        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "jwt.secret belum diisi. Set environment variable JWT_SECRET (atau properti jwt.secret) "
                    + "dengan string acak minimal 32 karakter. Contoh generate: "
                    + "openssl rand -base64 48");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret terlalu pendek (" + keyBytes.length + " byte). HS256 butuh minimal "
                    + MIN_SECRET_BYTES + " byte (256 bit).");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    // Token Generation
    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Token Parsing
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractRole(String token) {
        return parse(token).get(CLAIM_ROLE, String.class);
    }

    // Token Validation
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
