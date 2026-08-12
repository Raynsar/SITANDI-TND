package com.sitandi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Render halaman surat masuk (HTML shell).
 * Data surat diambil oleh JavaScript dari REST API /api/v1/inbox.
 */
@Controller
public class SuratMasukController {

    @GetMapping("/surat-masuk")
    public String suratMasuk() {
        return "user/surat-masuk";
    }
}
