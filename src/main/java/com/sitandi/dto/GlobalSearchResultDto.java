package com.sitandi.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hasil global search, dikelompokkan per kategori.
 * - personal: personel struktur organisasi (nama/NIP/username)
 * - opd: organisasi perangkat daerah (nama OPD)
 * - user: akun pengguna aplikasi (nama/username)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResultDto {

    private List<PersonalResult> personal;
    private List<OpdResult> opd;
    private List<UserResult> user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalResult {
        private Long id;
        private String nama;
        private String nip;
        private String username;
        private String jabatan;
        private Long bidangId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpdResult {
        private Long id;
        private String nama;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResult {
        private Long id;
        private String nama;
        private String username;
        private String role;
    }
}
