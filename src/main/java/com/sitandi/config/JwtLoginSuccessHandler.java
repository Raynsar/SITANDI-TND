package com.sitandi.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.dto.TndeUserDataDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * JWT Login Success Handler
 */
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtCookieService cookieService;
    private final com.sitandi.service.AuthService authService;
    private final com.sitandi.service.ActivityLogService activityLogService;

    public JwtLoginSuccessHandler(JwtUtil jwtUtil, JwtCookieService cookieService,
                                  com.sitandi.service.AuthService authService,
                                  com.sitandi.service.ActivityLogService activityLogService) {
        this.jwtUtil = jwtUtil;
        this.cookieService = cookieService;
        this.authService = authService;
        this.activityLogService = activityLogService;
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

        try {
            com.sitandi.model.Pengguna pengguna = authService.getByUsername(username);
            activityLogService.log(pengguna, "LOGIN_BERHASIL", "Login berhasil via form");
        } catch (Exception ignored) {
            // kalau user tidak ditemukan di tabel pengguna, tetap lanjut login
        }

        cookieService.tulis(response, jwtUtil.generateToken(username, role), contextPath);

        // Save TNDE Data to Session
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

        // Redirect Based on Role
        boolean isAdmin = role.contains("ADMIN");
        String target = isAdmin ? "/admin/dashboard" : "/organisasi/tree";
        response.sendRedirect(contextPath + target);
    }
}
