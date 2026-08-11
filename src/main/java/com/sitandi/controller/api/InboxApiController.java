package com.sitandi.controller.api;

import java.time.Year;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.dto.InboxResponseDto.SuratDto;
import com.sitandi.exception.TndeLoginException;
import com.sitandi.exception.TndeServerException;
import com.sitandi.service.InboxService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * REST API untuk mengambil data surat masuk dari TNDE.
 * Token TNDE diambil dari session (disimpan saat login berhasil via JwtLoginSuccessHandler).
 *
 * Response JSON:
 * - Sukses: { "tahun": 2026, "daftarSurat": [...] }
 * - Tanpa token TNDE: { "tahun": null, "daftarSurat": [], "info": "..." }
 * - Error TNDE: { "tahun": 2026, "daftarSurat": [], "error": "..." }
 */
@RestController
@RequestMapping("/api/v1/inbox")
@RequiredArgsConstructor
public class InboxApiController {

    private final InboxService inboxService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getInbox(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String tndeToken = (session != null) ? (String) session.getAttribute("tndeToken") : null;

        Map<String, Object> body = new LinkedHashMap<>();

        if (tndeToken == null || tndeToken.isBlank()) {
            body.put("tahun", null);
            body.put("daftarSurat", Collections.emptyList());
            body.put("info", "Fitur Surat Masuk hanya tersedia untuk akun yang masuk lewat sistem TNDE.");
            return ResponseEntity.ok(body);
        }

        int tahun = Year.now().getValue();
        body.put("tahun", tahun);

        try {
            List<SuratDto> daftarSurat = inboxService.getInbox(tndeToken, tahun);
            body.put("daftarSurat", daftarSurat);
        } catch (TndeLoginException ex) {
            session.invalidate();
            body.put("daftarSurat", Collections.emptyList());
            body.put("error", "Sesi TNDE telah berakhir. Silakan masuk ulang lewat TNDE untuk melihat surat masuk.");
        } catch (TndeServerException ex) {
            body.put("daftarSurat", Collections.emptyList());
            body.put("error", ex.getMessage());
        }

        return ResponseEntity.ok(body);
    }
}
