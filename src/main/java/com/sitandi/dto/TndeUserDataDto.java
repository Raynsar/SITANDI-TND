package com.sitandi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Struktur user data dalam response API TNDE.
 * Field menggunakan @JsonProperty untuk mapping nama field yang berbeda antara
 * API TNDE (snake_case) dan Java (camelCase).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TndeUserDataDto {

    @JsonProperty("user_id")
    private Integer userId;

    private String username;

    private String nama;

    private String nip;

    @JsonProperty("level_id")
    private String levelId;

    @JsonProperty("level_name")
    private String levelName;

    private Integer idou;

    @JsonProperty("idou_ref")
    private Integer idouRef;

    private String idstruktur;

    private String status;

    private String statusa;

    private Integer idsub;

    private String kodeinstansi;

    private String arsip;

    private Integer sstatus;

    private String eksekutif;

    @JsonProperty("status_surat")
    private String statusSurat;
}
