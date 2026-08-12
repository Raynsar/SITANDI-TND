package com.sitandi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body dari API login TNDE.
 * Mapping dengan response yang dikirim server TNDE di http://192.168.1.131:1323/api/login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TndeLoginResponseDto {

    private boolean success;

    private String message;

    /**
     * Data object berisi token JWT dan user information.
     * Hanya ada ketika success = true.
     */
    private TndeLoginDataDto data;

    /**
     * Inner class untuk menampung data login TNDE.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TndeLoginDataDto {

        private String token;

        private TndeUserDataDto user;
    }
}
