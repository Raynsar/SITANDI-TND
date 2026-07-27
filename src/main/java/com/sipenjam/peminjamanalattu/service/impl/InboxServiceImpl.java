package com.sipenjam.peminjamanalattu.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sipenjam.peminjamanalattu.dto.InboxResponseDto;
import com.sipenjam.peminjamanalattu.dto.InboxResponseDto.SuratDto;
import com.sipenjam.peminjamanalattu.exception.TndeLoginException;
import com.sipenjam.peminjamanalattu.exception.TndeServerException;
import com.sipenjam.peminjamanalattu.service.InboxService;

/**
 * Implementasi pemanggilan API inbox TNDE.
 * Konsisten dengan pola AuthServiceImpl yang sudah ada.
 */
@Service
public class InboxServiceImpl implements InboxService {

    private final RestTemplate restTemplate;
    private final String tndeInboxUrl;

    public InboxServiceImpl(RestTemplate restTemplate,
                            @Value("${tnde.inbox-url:http://192.168.1.131:1323/api/inbox}") String tndeInboxUrl) {
        this.restTemplate = restTemplate;
        this.tndeInboxUrl = tndeInboxUrl;
    }

    @Override
    public List<SuratDto> getInbox(String tndeToken, int tahun) {
        if (!StringUtils.hasText(tndeToken)) {
            throw new TndeLoginException("Token TNDE tidak tersedia, silakan login kembali");
        }

        String url = tndeInboxUrl + "?year=" + tahun;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tndeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        InboxResponseDto response;
        try {
            ResponseEntity<InboxResponseDto> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, InboxResponseDto.class);
            response = responseEntity.getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            // 401 — token invalid / expired
            throw new TndeLoginException("Sesi anda telah berakhir, silakan login kembali", ex);
        } catch (HttpStatusCodeException ex) {
            // 4xx / 5xx lainnya
            throw new TndeServerException("Gagal mengambil data surat dari server TNDE", ex);
        } catch (ResourceAccessException ex) {
            // Connection timeout / server unreachable
            throw new TndeServerException("Server TNDE tidak dapat dihubungi, silakan coba lagi nanti", ex);
        } catch (RestClientException ex) {
            // Error umum
            throw new TndeServerException("Gagal memproses respons dari server TNDE", ex);
        }

        if (response == null) {
            throw new TndeServerException("Server TNDE tidak memberikan respons yang valid");
        }
        if (!response.isSuccess()) {
            String message = StringUtils.hasText(response.getMessage())
                    ? response.getMessage()
                    : "Gagal mengambil data surat dari server TNDE";
            throw new TndeServerException(message);
        }

        // Data response: { "data": { "items": [ ... ] } }
        if (response.getData() == null || response.getData().getItems() == null) {
            return Collections.emptyList();
        }
        return response.getData().getItems();
    }
}

