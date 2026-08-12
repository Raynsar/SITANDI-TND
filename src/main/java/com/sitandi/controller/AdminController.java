package com.sitandi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Render halaman area admin. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    /** Halaman kelola pengguna — data diambil via fetch JSON oleh JavaScript. */
    @GetMapping("/pengguna")
    public String pengguna() {
        return "admin/pengguna";
    }
}

