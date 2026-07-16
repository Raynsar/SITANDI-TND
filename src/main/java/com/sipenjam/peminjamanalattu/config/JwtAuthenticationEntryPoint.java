package com.sipenjam.peminjamanalattu.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Entry point untuk chain /api/**: membalas 401 + body JSON, BUKAN redirect ke
 * /login. Klien API (fetch/Postman) tidak melakukan navigasi browser, jadi
 * redirect 302 ke halaman HTML hanya akan membingungkan — fetch akan mengikuti
 * redirect diam-diam dan mengembalikan HTML 200, bukan error yang bisa dideteksi.
 *
 * Format body sengaja dibuat sama dengan GlobalExceptionHandler agar klien cukup
 * memparsing satu bentuk error.
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        // Pesan generik: jangan bocorkan apakah token tidak ada, kedaluwarsa, atau
        // signature-nya salah — detail itu membantu penyerang menebak-nebak.
        body.put("message", "Autentikasi diperlukan atau token tidak valid");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
