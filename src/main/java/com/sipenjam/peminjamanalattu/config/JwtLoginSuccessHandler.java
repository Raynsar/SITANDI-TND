package com.sipenjam.peminjamanalattu.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Dijalankan setelah form login memverifikasi username+password. Tugasnya menerbitkan
 * JWT, menaruhnya di cookie http-only, lalu mengarahkan sesuai role.
 *
 * Di sinilah status "akun sudah disetujui admin" diperiksa — SETELAH password terbukti
 * benar, bukan sebelumnya. Kalau pemeriksaan ini dipindah ke UserDetails
 * ({@code User.disabled()}), Spring akan menolak akun nonaktif sebelum password dicek,
 * sehingga siapa pun bisa menembak username dengan password ngawur dan membedakan
 * "belum disetujui" dari "tidak terdaftar" — itu user enumeration. Lihat catatan di
 * AuthServiceImpl#loadUserByUsername.
 */
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtCookieService cookieService;
    private final AuthService authService;

    public JwtLoginSuccessHandler(JwtUtil jwtUtil, JwtCookieService cookieService,
                                  AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.cookieService = cookieService;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String contextPath = request.getContextPath();
        Pengguna pengguna = authService.getByUsername(authentication.getName());

        if (!pengguna.isAktif()) {
            // Password benar, tapi admin belum menyetujui akun ini. Tidak ada token
            // yang diterbitkan, jadi user tetap anonim.
            response.sendRedirect(contextPath + "/login?pending");
            return;
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_" + Role.USER.name());

        cookieService.tulis(response, jwtUtil.generateToken(pengguna.getUsername(), role), contextPath);

        // getContextPath() wajib: sendRedirect("/...") relatif ke root host, bukan ke
        // context path, jadi tanpa ini redirect 404 saat deploy WAR non-root.
        String target = pengguna.getRole() == Role.ADMIN ? "/admin/dashboard" : "/katalog";
        response.sendRedirect(contextPath + target);
    }
}
