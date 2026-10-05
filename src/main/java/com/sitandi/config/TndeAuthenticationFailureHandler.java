package com.sitandi.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TndeAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final com.sitandi.service.ActivityLogService activityLogService;

    public TndeAuthenticationFailureHandler(com.sitandi.service.ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "Username atau password salah";
        }
        String username = request.getParameter("username");
        try {
            activityLogService.log(null, "LOGIN_GAGAL",
                    "Login gagal untuk username '" + username + "': " + message);
        } catch (Exception ignored) { }
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String url = request.getContextPath() + "/login?error=true&message=" + encodedMessage;
        response.sendRedirect(url);
    }
}
