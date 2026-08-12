package com.sitandi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sitandi.repository.BidangRepository;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.PenggunaRepository;
import com.sitandi.repository.WilayahRepository;
import com.sitandi.service.AuthService;

import lombok.RequiredArgsConstructor;

/** Render halaman area admin. */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final WilayahRepository wilayahRepository;
    private final OpdRepository opdRepository;
    private final BidangRepository bidangRepository;
    private final JabatanStrukturRepository jabatanStrukturRepository;
    private final PenggunaRepository penggunaRepository;
    private final AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalWilayah", wilayahRepository.count());
        model.addAttribute("totalOpd", opdRepository.count());
        model.addAttribute("totalBidang", bidangRepository.count());
        model.addAttribute("totalPersonal", jabatanStrukturRepository.count());
        model.addAttribute("totalPengguna", penggunaRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/pengguna")
    public String pengguna(Model model) {
        model.addAttribute("daftarPengguna", authService.daftarSemuaPengguna());
        model.addAttribute("jumlahMenungguAktivasi", authService.daftarMenungguAktivasi().size());
        return "admin/pengguna";
    }
}
