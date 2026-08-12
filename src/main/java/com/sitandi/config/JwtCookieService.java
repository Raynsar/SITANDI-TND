package com.sitandi.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Cookie Management Service
 */
@Component
public class JwtCookieService {

    /** Nama cookie berisi JWT. */
    public static final String COOKIE_NAME = "SITANDI_TOKEN";

    private final long expirationMs;
    private final boolean secure;

    public JwtCookieService(@Value("${jwt.expiration-ms:3600000}") long expirationMs,
                            @Value("${jwt.cookie-secure:false}") boolean secure) {
        this.expirationMs = expirationMs;
        this.secure = secure;
    }

    // Write Cookie
    public void tulis(HttpServletResponse response, String token, String contextPath) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(pathAman(contextPath))
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // Delete Cookie
    public void hapus(HttpServletResponse response, String contextPath) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(pathAman(contextPath))
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public Optional<String> baca(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    // Safe Path Helper
    private String pathAman(String contextPath) {
        return (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
    }
}
