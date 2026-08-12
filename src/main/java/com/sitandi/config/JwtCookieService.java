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
 * Menaruh & menghapus JWT pada cookie http-only.
 *
 * Kenapa cookie, bukan sessionStorage: halaman ini di-render server (Thymeleaf).
 * Navigasi browser — klik link, ketik URL, refresh — tidak bisa menyertakan header
 * Authorization, jadi token HARUS ikut otomatis, dan cookie satu-satunya mekanisme
 * yang melakukan itu.
 *
 * Kenapa http-only: JavaScript tidak bisa membaca cookie ini, sehingga XSS tidak
 * bisa mencuri tokennya. Ini alasan token TIDAK disimpan di sessionStorage, yang
 * justru terbaca oleh script apa pun yang berhasil disuntikkan ke halaman.
 *
 * Konsekuensi yang harus disadari: karena cookie ikut terkirim otomatis di setiap
 * request lintas-situs yang diizinkan SameSite, proteksi CSRF menjadi WAJIB —
 * lihat SecurityConfig.
 */
@Component
public class JwtCookieService {

    /** Nama cookie berisi JWT. */
    public static final String COOKIE_NAME = "SIPENJAM_TOKEN";

    private final long expirationMs;
    private final boolean secure;

    public JwtCookieService(@Value("${jwt.expiration-ms:3600000}") long expirationMs,
                            @Value("${jwt.cookie-secure:false}") boolean secure) {
        this.expirationMs = expirationMs;
        this.secure = secure;
    }

    /**
     * @param contextPath dari request.getContextPath(); wajib dipakai sebagai Path
     *                    cookie. Aplikasi berjalan di /sipenjam, bukan root — cookie
     *                    ber-Path "/" akan ikut terkirim ke aplikasi lain di Tomcat
     *                    yang sama, dan Path yang salah membuat cookie tidak pernah
     *                    dikirim balik sehingga user seperti tidak pernah login.
     */
    public void tulis(HttpServletResponse response, String token, String contextPath) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                // Lax: cookie tetap terkirim saat user mengklik link menuju aplikasi
                // (navigasi top-level GET), tapi tidak pada POST lintas-situs.
                .sameSite("Lax")
                .path(pathAman(contextPath))
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Menghapus cookie dengan menimpanya memakai maxAge 0. */
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

    /** Context path kosong (deploy di root) harus menjadi "/" agar cookie valid. */
    private String pathAman(String contextPath) {
        return (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
    }
}
