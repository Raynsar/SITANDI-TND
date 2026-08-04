package com.sitandi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body ke API login TNDE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TndeLoginRequestDto {

    private String username;
    private String password;
}
