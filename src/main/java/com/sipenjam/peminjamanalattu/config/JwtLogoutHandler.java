package com.sipenjam.peminjamanalattu.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logout = menghapus cookie JWT dari browser.
 *
 * PENTING, dan ini konsekuensi nyata dari autentikasi stateless: token yang sudah
 * terbit TIDAK bisa dibatalkan. Server tidak menyimpan daftar sesi aktif, jadi kalau
 * seseorang sempat menyalin nilai token sebelum logout, token itu tetap sah sampai
 * exp-nya lewat (lihat jwt.expiration-ms, default 1 jam). Logout hanya membuat
 * browser ini berhenti mengirimkannya.
 *
 * Kalau pencabutan seketika benar-benar dibutuhkan, pilihannya: perpendek masa
 * berlaku token, atau simpan daftar token yang dicabut di server — dan yang kedua
 * berarti aplikasi tidak stateless lagi.
 */
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtCookieService cookieService;

    public JwtLogoutHandler(JwtCookieService cookieService) {
        this.cookieService = cookieService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        cookieService.hapus(response, request.getContextPath());
    }
}
