package com.sipenjam.peminjamanalattu.controller;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sipenjam.peminjamanalattu.dto.InboxResponseDto.SuratDto;
import com.sipenjam.peminjamanalattu.exception.TndeLoginException;
import com.sipenjam.peminjamanalattu.exception.TndeServerException;
import com.sipenjam.peminjamanalattu.service.InboxService;

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
        if (session == null) {
            return "redirect:/login?session-expired";
        }

        String tndeToken = (String) session.getAttribute("tndeToken");
        if (tndeToken == null || tndeToken.isBlank()) {
            return "redirect:/login?session-expired";
        }

        int tahun = Year.now().getValue();

        try {
            List<SuratDto> daftarSurat = inboxService.getInbox(tndeToken, tahun);
            model.addAttribute("daftarSurat", daftarSurat);
            model.addAttribute("tahun", tahun);
        } catch (TndeLoginException ex) {
            // Token expired / invalid → redirect ke login
            session.invalidate();
            return "redirect:/login?session-expired";
        } catch (TndeServerException ex) {
            // Server TNDE error → tampilkan pesan di halaman, jangan crash
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("daftarSurat", List.of());
            model.addAttribute("tahun", tahun);
        }

        return "user/surat-masuk";
    }
}

