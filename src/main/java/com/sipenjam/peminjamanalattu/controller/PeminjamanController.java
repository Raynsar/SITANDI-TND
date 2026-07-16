package com.sipenjam.peminjamanalattu.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sipenjam.peminjamanalattu.service.PeminjamanService;

import lombok.RequiredArgsConstructor;

/**
 * Render halaman riwayat peminjaman milik pengguna yang sedang login.
 */
@Controller
@RequiredArgsConstructor
public class PeminjamanController {

    private final PeminjamanService peminjamanService;

    @GetMapping("/riwayat")
    public String riwayat(Model model, Principal principal) {
        model.addAttribute("riwayatPeminjaman", peminjamanService.riwayat(principal.getName()));
        return "user/riwayat";
    }
}
