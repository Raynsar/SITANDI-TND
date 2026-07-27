package com.sipenjam.peminjamanalattu.config;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.sipenjam.peminjamanalattu.dto.TndeLoginResponseDto;
import com.sipenjam.peminjamanalattu.exception.TndeLoginException;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.service.AuthService;

@Component
public class TndeAuthenticationProvider implements AuthenticationProvider {

    private final AuthService authService;

    public TndeAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        String password = credentials == null ? "" : credentials.toString();

        TndeLoginResponseDto response;
        try {
            response = authService.loginTnde(username, password);
        } catch (TndeLoginException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }

        if (response == null || response.getData() == null || response.getData().getUser() == null) {
            throw new BadCredentialsException("Login TNDE gagal");
        }

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
