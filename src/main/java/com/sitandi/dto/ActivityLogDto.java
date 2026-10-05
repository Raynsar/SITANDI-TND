package com.sitandi.dto;

import java.time.LocalDateTime;

import com.sitandi.model.ActivityLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {

    private Long id;
    private String pengguna; // username, atau '-' kalau null
    private String aksi;
    private String deskripsi;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static ActivityLogDto dari(ActivityLog log) {
        return ActivityLogDto.builder()
                .id(log.getId())
                .pengguna(log.getPengguna() != null ? log.getPengguna().getUsername() : "-")
                .aksi(log.getAksi())
                .deskripsi(log.getDeskripsi())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
