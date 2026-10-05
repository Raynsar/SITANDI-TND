package com.sitandi.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.dto.ActivityLogDto;
import com.sitandi.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

/** Admin-only (diatur di SecurityConfig): GET /api/v1/activity-log */
@RestController
@RequestMapping("/api/v1/activity-log")
@RequiredArgsConstructor
public class ActivityLogApiController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public List<ActivityLogDto> daftar(
            @RequestParam(required = false) String aksi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dari,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sampai) {
        return activityLogService.cari(aksi, dari, sampai);
    }
}
