package com.sitandi.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//logout

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
