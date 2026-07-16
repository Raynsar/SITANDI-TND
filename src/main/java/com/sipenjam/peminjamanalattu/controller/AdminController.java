package com.sipenjam.peminjamanalattu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sipenjam.peminjamanalattu.service.AlatService;
import com.sipenjam.peminjamanalattu.service.AuthService;
import com.sipenjam.peminjamanalattu.service.PeminjamanService;

import lombok.RequiredArgsConstructor;

/**
 * Render halaman area admin (dashboard, persetujuan, inventaris).
 * Akses /admin/** dibatasi ROLE_ADMIN di SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PeminjamanService peminjamanService;
    private final AlatService alatService;
    private final AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("jumlahAktif", peminjamanService.jumlahAktif());
        model.addAttribute("jumlahMenunggu", peminjamanService.jumlahMenunggu());
        model.addAttribute("totalAlat", alatService.totalAlat());
        model.addAttribute("logAktivitas", peminjamanService.logAktivitas());
        return "admin/dashboard";
    }

    @GetMapping("/persetujuan")
    public String persetujuan(Model model) {
        model.addAttribute("daftarMenunggu", peminjamanService.daftarMenunggu());
        return "admin/persetujuan";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("daftarAlat", alatService.getSemuaAlat());
        return "admin/inventory";
    }

    @GetMapping("/tambah-alat")
    public String tambahAlat(Model model) {
        model.addAttribute("daftarKategori", alatService.getSemuaKategori());
        return "admin/tambah-alat";
    }

    @GetMapping("/pengembalian")
    public String pengembalian(Model model) {
        model.addAttribute("daftarPengembalian", peminjamanService.daftarPengembalian());
        return "admin/pengembalian";
    }

    @GetMapping("/pengguna")
    public String pengguna(Model model) {
        model.addAttribute("daftarPengguna", authService.daftarSemuaPengguna());
        model.addAttribute("jumlahMenungguAktivasi", authService.daftarMenungguAktivasi().size());
        return "admin/pengguna";
    }
}
