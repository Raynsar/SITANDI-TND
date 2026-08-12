package com.sitandi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body dari API TNDE GET /api/inbox.
 *
 * Struktur response asli:
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": {
 *     "items": [ ... daftar surat ... ]
 *   }
 * }
 * </pre>
 *
 * Field "data" adalah OBJECT (bukan array langsung), berisi field "items"
 * yang merupakan array daftar surat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuratMasukResponseDto {

    private boolean success;

    private String message;

    /** Data wrapper object — bukan array langsung. */
    private SuratMasukDataDto data;

    /**
     * Wrapper untuk field "data" yang berisi array "items".
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuratMasukDataDto {

        /** Array daftar surat masuk. */
        private List<SuratDto> items;
    }

    /**
     * Data surat masuk / disposisi dari TNDE.
     * Field menggunakan @JsonProperty untuk mapping field snake_case dari API TNDE.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuratDto {

        private String id;

        @JsonProperty("nomor_surat")
        private String nomorSurat;

        private String perihal;

        private String tanggal;

        @JsonProperty("pengirim")
        private String pengirim;

        private String status;

        @JsonProperty("tujuan")
        private String tujuan;

        @JsonProperty("isi_disposisi")
        private String isiDisposisi;

        @JsonProperty("lampiran")
        private String lampiran;

        @JsonProperty("tgl_surat")
        private String tglSurat;

        @JsonProperty("tgl_terima")
        private String tglTerima;
    }
}

