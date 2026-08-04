package com.sitandi.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.model.Role;
import com.sitandi.service.AuthService;

/**
 * AuthenticationProvider yang mencoba login ke API TNDE eksternal.
 *
 * TIDAK pakai @Component — bean-nya dibuat manual di SecurityConfig bersama
 * DaoAuthenticationProvider, agar ProviderManager berisi kedua provider dan
 * bisa fallback ke lokal jika TNDE gagal.
 *
 * Kalau TNDE gagal (koneksi, user tidak dikenal, dll), method authenticate()
 * return null sebagai sinyal ke ProviderManager untuk mencoba provider berikutnya.
 */
public class TndeAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(TndeAuthenticationProvider.class);

    private final AuthService authService;

    public TndeAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        String password = credentials == null ? "" : credentials.toString();

        log.info("[TNDE Provider] Mencoba login TNDE untuk user: '{}'", username);

        TndeLoginResponseDto response;
        try {
            response = authService.loginTnde(username, password);
        } catch (Exception ex) {
            log.warn("[TNDE Provider] Login TNDE GAGAL untuk user: '{}' — {} | type: {}",
                    username, ex.getMessage(), ex.getClass().getName());
            // return null = ProviderManager lanjut ke provider berikutnya (DaoAuthenticationProvider lokal)
            return null;
        }

        if (response == null || response.getData() == null || response.getData().getUser() == null) {
            log.warn("[TNDE Provider] Response TNDE tidak lengkap untuk user: '{}' — fallback ke lokal", username);
            return null;
        }

        log.info("[TNDE Provider] Login TNDE BERHASIL untuk user: '{}'", username);

        Role role = mapLevelNameToRole(response.getData().getUser().getLevelName());
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(authority));
        token.setDetails(response.getData());
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Role mapLevelNameToRole(String levelName) {
        if (levelName == null) {
            return Role.USER;
        }
        String lower = levelName.toLowerCase();
        if (lower.contains("admin")) {
            return Role.ADMIN;
        }
        return Role.USER;
    }
}

