package com.sitandi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.dto.TndeLoginResponseDto.TndeLoginDataDto;
import com.sitandi.dto.TndeUserDataDto;
import com.sitandi.exception.TndeLoginException;
import com.sitandi.service.AuthService;

/**
 * Unit test TndeAuthenticationProvider.
 *
 * Test Double: Mock AuthService (verifikasi pemanggilan loginTnde), Stub (respons TNDE),
 * dan Dummy (null untuk AuthService pada test supports() yang tidak memakainya).
 */
@ExtendWith(MockitoExtension.class)
class TndeAuthenticationProviderTest {

    @Mock
    private AuthService authService;

    private TndeLoginResponseDto response(String levelName) {
        TndeUserDataDto user = TndeUserDataDto.builder().username("tnde1").nama("Nama TNDE").levelName(levelName).build();
        return TndeLoginResponseDto.builder().success(true).message("OK")
                .data(TndeLoginDataDto.builder().token("jwt-tnde").user(user).build()).build();
    }

    @Test
    @DisplayName("authenticate: level 'Administrator' -> token terautentikasi ROLE_ADMIN, details berisi data TNDE")
    void authenticate_admin() {
        TndeLoginResponseDto resp = response("Administrator");
        when(authService.loginTnde("tnde1", "rahasia")).thenReturn(resp);
        TndeAuthenticationProvider provider = new TndeAuthenticationProvider(authService);

        Authentication hasil = provider.authenticate(new UsernamePasswordAuthenticationToken("tnde1", "rahasia"));

        assertThat(hasil).isNotNull();
        assertThat(hasil.isAuthenticated()).isTrue();
        assertThat(hasil.getName()).isEqualTo("tnde1");
        assertThat(hasil.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(hasil.getDetails()).isSameAs(resp.getData());
    }

    @Test
    @DisplayName("authenticate: level non-admin -> ROLE_USER")
    void authenticate_nonAdmin() {
        when(authService.loginTnde("tnde1", "rahasia")).thenReturn(response("Staff"));

        Authentication hasil = new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "rahasia"));

        assertThat(hasil.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("authenticate: level_name null -> ROLE_USER")
    void authenticate_levelNull() {
        when(authService.loginTnde("tnde1", "rahasia")).thenReturn(response(null));

        Authentication hasil = new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "rahasia"));

        assertThat(hasil.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("authenticate: TNDE gagal (exception) -> null agar fallback ke provider lokal")
    void authenticate_tndeGagal_null() {
        when(authService.loginTnde("tnde1", "salah")).thenThrow(new TndeLoginException("Password salah"));

        Authentication hasil = new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "salah"));

        assertThat(hasil).isNull();
    }

    @Test
    @DisplayName("authenticate: respons null -> null")
    void authenticate_responsNull() {
        when(authService.loginTnde("tnde1", "x")).thenReturn(null);

        assertThat(new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "x"))).isNull();
    }

    @Test
    @DisplayName("authenticate: data null -> null")
    void authenticate_dataNull() {
        when(authService.loginTnde("tnde1", "x"))
                .thenReturn(TndeLoginResponseDto.builder().success(true).data(null).build());

        assertThat(new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "x"))).isNull();
    }

    @Test
    @DisplayName("authenticate: user null -> null")
    void authenticate_userNull() {
        when(authService.loginTnde("tnde1", "x")).thenReturn(TndeLoginResponseDto.builder().success(true)
                .data(TndeLoginDataDto.builder().token("t").user(null).build()).build());

        assertThat(new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", "x"))).isNull();
    }

    @Test
    @DisplayName("authenticate: credentials null dikirim sebagai string kosong")
    void authenticate_credentialsNull() {
        when(authService.loginTnde("tnde1", "")).thenReturn(response("Staff"));

        new TndeAuthenticationProvider(authService)
                .authenticate(new UsernamePasswordAuthenticationToken("tnde1", null));

        verify(authService).loginTnde("tnde1", "");
    }

    @Test
    @DisplayName("supports: hanya UsernamePasswordAuthenticationToken (AuthService = Dummy null)")
    void supports() {
        TndeAuthenticationProvider provider = new TndeAuthenticationProvider(null); // Dummy

        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        assertThat(provider.supports(Object.class)).isFalse();
    }
}
