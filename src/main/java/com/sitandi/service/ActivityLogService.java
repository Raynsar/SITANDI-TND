package com.sitandi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sitandi.model.ActivityLog;
import com.sitandi.model.Pengguna;
import com.sitandi.repository.ActivityLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    /** Catat satu aksi ke tabel activity_log. */
    public void log(Pengguna pengguna, String aksi, String deskripsi) {
        repository.save(ActivityLog.builder()
                .pengguna(pengguna)
                .aksi(aksi)
                .deskripsi(deskripsi)
                .ipAddress(ambilIp())
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<com.sitandi.dto.ActivityLogDto> cari(String aksi, LocalDate dari, LocalDate sampai) {
        LocalDateTime start = dari != null ? dari.atStartOfDay() : null;
        LocalDateTime end = sampai != null ? sampai.atTime(LocalTime.MAX) : null;

        List<ActivityLog> hasil;
        if (aksi != null && !aksi.isBlank() && start != null && end != null) {
            hasil = repository.findByAksiAndCreatedAtBetweenOrderByCreatedAtDesc(aksi, start, end);
        } else if (aksi != null && !aksi.isBlank()) {
            hasil = repository.findByAksiOrderByCreatedAtDesc(aksi);
        } else if (start != null && end != null) {
            hasil = repository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        } else {
            hasil = repository.findAllByOrderByCreatedAtDesc();
        }
        return hasil.stream().map(com.sitandi.dto.ActivityLogDto::dari).toList();
    }

    private String ambilIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String xff = req.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
