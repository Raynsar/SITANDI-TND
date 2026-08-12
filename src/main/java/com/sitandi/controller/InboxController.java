package com.sitandi.controller;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sitandi.dto.InboxResponseDto.SuratDto;
import com.sitandi.exception.TndeLoginException;
import com.sitandi.exception.TndeServerException;
import com.sitandi.service.InboxService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Menampilkan halaman surat masuk dari API TNDE inbox.
 * Token JWT TNDE diambil dari session (disimpan saat login berhasil).
 */
@Controller
public class InboxController {

    private final InboxService inboxService;

    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping("/surat-masuk")
    public String suratMasuk(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String tndeToken = (session != null) ? (String) session.getAttribute("tndeToken") : null;

        if (tndeToken == null || tndeToken.isBlank()) {
            // Akun login lokal (atau sesi TNDE sudah berakhir) — tampilkan halaman
            // dengan pesan informatif, jangan redirect ke /login. JWT cookie masih
            // valid sehingga user tidak benar-benar logout; redirect ke /login hanya
            // membuat user bingung karena JWT tidak ikut terhapus.
            int tahun = Year.now().getValue();
            model.addAttribute("tahun", null);
            return "user/surat-masuk";
        }

        int tahun = Year.now().getValue();

        try {
            List<SuratDto> daftarSurat = inboxService.getInbox(tndeToken, tahun);
            model.addAttribute("daftarSurat", daftarSurat);
            model.addAttribute("tahun", tahun);
        } catch (TndeLoginException ex) {
            // Token expired / invalid — render halaman dengan pesan; user tetap
            // bisa navigasi ke menu lain tanpa terlihat seperti 'logout'.
            session.invalidate();
            model.addAttribute("error", "Sesi TNDE telah berakhir. Silakan masuk ulang lewat TNDE untuk melihat surat masuk.");
            model.addAttribute("daftarSurat", List.of());
            model.addAttribute("tahun", tahun);
        } catch (TndeServerException ex) {
            // Server TNDE error — tampilkan pesan di halaman, jangan crash
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("daftarSurat", List.of());
            model.addAttribute("tahun", tahun);
        }

        return "user/surat-masuk";
    }
}

