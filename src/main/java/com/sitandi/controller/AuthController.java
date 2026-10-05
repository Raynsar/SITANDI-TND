package com.sitandi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Render halaman terkait akun. Proses POST /login ditangani Spring Security
 * (JwtLoginSuccessHandler yang menerbitkan cookie JWT); submit form register &
 * ganti password ditangani JS lewat /api/v1/auth/**.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/ganti-password")
    public String gantiPassword() {
        return "auth/ganti-password";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "auth/reset-password";
    }
}
