package com.sitandi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sitandi.dto.SuratMasukResponseDto;
import com.sitandi.dto.SuratMasukResponseDto.SuratDto;
import com.sitandi.dto.SuratMasukResponseDto.SuratMasukDataDto;
import com.sitandi.exception.TndeLoginException;
import com.sitandi.exception.TndeServerException;

/**
 * Unit test Surat Masuk (integrasi API inbox TNDE).
 *
 * Test Double: Mock RestTemplate (server TNDE tidak dipanggil sungguhan; header Bearer
 * diverifikasi) dan Stub (response dan exception HTTP disiapkan).
 */
@ExtendWith(MockitoExtension.class)
class SuratMasukServiceImplTest {

    private static final String INBOX_URL = "http://tnde.test/api/inbox";
    private static final String URL_2025 = INBOX_URL + "?year=2025";

    @Mock
    private RestTemplate restTemplate;

    private SuratMasukServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SuratMasukServiceImpl(restTemplate, INBOX_URL);
    }

    private void stubResponse(SuratMasukResponseDto body) {
        when(restTemplate.exchange(eq(URL_2025), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(SuratMasukResponseDto.class)))
                .thenReturn(new ResponseEntity<SuratMasukResponseDto>(body, HttpStatus.OK));
    }

    private void stubThrow(Throwable t) {
        when(restTemplate.exchange(eq(URL_2025), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(SuratMasukResponseDto.class)))
                .thenThrow(t);
    }

    @Test
    @DisplayName("ambilSuratMasuk sukses: daftar surat dikembalikan dan header Bearer terkirim")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void ambilSuratMasuk_sukses() {
        SuratDto surat = SuratDto.builder().id("1").nomorSurat("001/TEST/2025").perihal("Undangan").build();
        stubResponse(SuratMasukResponseDto.builder().success(true).message("OK")
                .data(SuratMasukDataDto.builder().items(List.of(surat)).build()).build());

        List<SuratDto> hasil = service.ambilSuratMasuk("token-tnde", 2025);

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getNomorSurat()).isEqualTo("001/TEST/2025");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(URL_2025), eq(HttpMethod.GET), captor.capture(),
                eq(SuratMasukResponseDto.class));
        assertThat(captor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer token-tnde");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("ambilSuratMasuk: token kosong ditolak tanpa memanggil TNDE")
    void ambilSuratMasuk_tokenKosong(String token) {
        assertThatThrownBy(() -> service.ambilSuratMasuk(token, 2025))
                .isInstanceOf(TndeLoginException.class)
                .hasMessageContaining("Token TNDE tidak tersedia");

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("ambilSuratMasuk: HTTP 401 -> TndeLoginException (sesi berakhir)")
    void ambilSuratMasuk_unauthorized() {
        stubThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized",
                HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeLoginException.class)
                .hasMessageContaining("Sesi anda telah berakhir");
    }

    @Test
    @DisplayName("ambilSuratMasuk: HTTP 500 -> TndeServerException")
    void ambilSuratMasuk_serverError() {
        stubThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error",
                HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Gagal mengambil data surat dari server TNDE");
    }

    @Test
    @DisplayName("ambilSuratMasuk: HTTP 404 (4xx selain 401) -> TndeServerException")
    void ambilSuratMasuk_clientErrorLain() {
        stubThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Gagal mengambil data surat dari server TNDE");
    }

    @Test
    @DisplayName("ambilSuratMasuk: server tidak terjangkau -> TndeServerException")
    void ambilSuratMasuk_timeout() {
        stubThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessageContaining("tidak dapat dihubungi");
    }

    @Test
    @DisplayName("ambilSuratMasuk: RestClientException umum -> TndeServerException")
    void ambilSuratMasuk_restClientException() {
        stubThrow(new RestClientException("gagal parse"));

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Gagal memproses respons dari server TNDE");
    }

    @Test
    @DisplayName("ambilSuratMasuk: body null -> TndeServerException")
    void ambilSuratMasuk_bodyNull() {
        stubResponse(null);

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Server TNDE tidak memberikan respons yang valid");
    }

    @Test
    @DisplayName("ambilSuratMasuk: success=false dengan pesan -> pesan TNDE dipakai")
    void ambilSuratMasuk_successFalseDenganPesan() {
        stubResponse(SuratMasukResponseDto.builder().success(false).message("Tahun tidak valid").build());

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Tahun tidak valid");
    }

    @Test
    @DisplayName("ambilSuratMasuk: success=false tanpa pesan -> pesan default")
    void ambilSuratMasuk_successFalseTanpaPesan() {
        stubResponse(SuratMasukResponseDto.builder().success(false).message(" ").build());

        assertThatThrownBy(() -> service.ambilSuratMasuk("token-tnde", 2025))
                .isInstanceOf(TndeServerException.class)
                .hasMessage("Gagal mengambil data surat dari server TNDE");
    }

    @Test
    @DisplayName("ambilSuratMasuk: data null -> list kosong")
    void ambilSuratMasuk_dataNull_kosong() {
        stubResponse(SuratMasukResponseDto.builder().success(true).data(null).build());

        assertThat(service.ambilSuratMasuk("token-tnde", 2025)).isEmpty();
    }

    @Test
    @DisplayName("ambilSuratMasuk: items null -> list kosong")
    void ambilSuratMasuk_itemsNull_kosong() {
        stubResponse(SuratMasukResponseDto.builder().success(true)
                .data(SuratMasukDataDto.builder().items(null).build()).build());

        assertThat(service.ambilSuratMasuk("token-tnde", 2025)).isEmpty();
    }
}
