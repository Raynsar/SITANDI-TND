package com.sipenjam.peminjamanalattu.config;

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
 * Pembuatan & validasi JWT untuk REST API.
 *
 * Payload token HANYA berisi username (subject), role, iat, dan exp. JANGAN
 * menambahkan data sensitif: payload JWT cuma di-encode Base64URL, BUKAN
 * dienkripsi, jadi siapa pun yang memegang token bisa membacanya.
 */
@Component
public class JwtUtil {

    /** Nama claim untuk role. Sengaja bukan "authorities" agar tidak tertukar dengan format Spring. */
    private static final String CLAIM_ROLE = "role";

    /** HS256 mensyaratkan key >= 256 bit; di bawah itu jjwt menolak dengan WeakKeyException. */
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        // Fail-fast saat startup: lebih baik aplikasi menolak jalan daripada diam-diam
        // memakai key lemah/default yang bisa dipakai menempa token.
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

    /**
     * @param username dipakai sebagai subject token
     * @param role     format Spring ("ROLE_ADMIN"/"ROLE_USER")
     */
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

    /**
     * Memverifikasi signature + masa berlaku token lalu mengembalikan payload-nya.
     *
     * @throws JwtException bila signature salah, token kedaluwarsa, atau formatnya rusak
     */
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

    /** Varian parse() yang tidak melempar; dipakai bila cukup butuh valid/tidak. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
