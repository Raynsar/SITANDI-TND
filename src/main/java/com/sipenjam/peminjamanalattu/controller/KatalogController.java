package com.sipenjam.peminjamanalattu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sipenjam.peminjamanalattu.service.AlatService;

import lombok.RequiredArgsConstructor;

/**
 * Render halaman katalog alat & form pengajuan peminjaman (Thymeleaf).
 */
@Controller
@RequiredArgsConstructor
public class KatalogController {

    private final AlatService alatService;

    @GetMapping("/katalog")
    public String katalog(Model model) {
        model.addAttribute("daftarAlat", alatService.getSemuaAlat());
        return "user/katalog";
    }

    @GetMapping("/form-pinjam")
    public String formPinjam(@RequestParam(name = "alatId", required = false) Long alatId, Model model) {
        model.addAttribute("alatId", alatId);
        return "user/form-pinjam";
    }
}
