package com.sipenjam.peminjamanalattu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Render halaman struktur organisasi (tree OPD se-Jatim).
 * Halaman terpisah dari modul peminjaman yang memakai Tailwind — template ini
 * menggunakan Tabler CDN.
 */
@Controller
@RequestMapping("/organisasi")
public class OrganisasiController {

    @GetMapping("/tree")
    public String tree() {
        return "organisasi/tree";
    }
}

