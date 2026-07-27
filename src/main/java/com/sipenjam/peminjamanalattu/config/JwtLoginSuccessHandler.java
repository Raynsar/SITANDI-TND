package com.sipenjam.peminjamanalattu.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.sipenjam.peminjamanalattu.dto.TndeLoginResponseDto;
import com.sipenjam.peminjamanalattu.dto.TndeUserDataDto;
import com.sipenjam.peminjamanalattu.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        cookieService.tulis(response, jwtUtil.generateToken(username, role), contextPath);

        // Simpan data TNDE ke session untuk digunakan fitur lain (misal: Surat Masuk)
        Object details = authentication.getDetails();
        if (details instanceof TndeLoginResponseDto.TndeLoginDataDto tndeData) {
            TndeUserDataDto userData = tndeData.getUser();
            HttpSession session = request.getSession(true);
            session.setAttribute("username", userData.getUsername());
            session.setAttribute("nama", userData.getNama());
            session.setAttribute("levelName", userData.getLevelName());
            session.setAttribute("kodeInstansi", userData.getKodeinstansi());
            session.setAttribute("tndeToken", tndeData.getToken());
        }

        // getContextPath() wajib: sendRedirect("/...") relatif ke root host, bukan ke
        // context path, jadi tanpa ini redirect 404 saat deploy WAR non-root.
        boolean isAdmin = role.contains("ADMIN");
        String target = isAdmin ? "/admin/dashboard" : "/katalog";
        response.sendRedirect(contextPath + target);
    }
}
