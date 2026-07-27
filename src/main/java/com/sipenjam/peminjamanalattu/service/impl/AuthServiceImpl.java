package com.sipenjam.peminjamanalattu.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sipenjam.peminjamanalattu.dto.GantiPasswordRequestDto;
import com.sipenjam.peminjamanalattu.dto.PenggunaDto;
import com.sipenjam.peminjamanalattu.dto.RegisterRequestDto;
import com.sipenjam.peminjamanalattu.dto.TndeLoginRequestDto;
import com.sipenjam.peminjamanalattu.dto.TndeLoginResponseDto;
import com.sipenjam.peminjamanalattu.dto.TndeUserDataDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.exception.TndeLoginException;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.repository.PenggunaRepository;
import com.sipenjam.peminjamanalattu.service.AuthService;


/**
 * Implementasi otentikasi & pengelolaan akun.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final PenggunaRepository penggunaRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String tndeLoginUrl;

    public AuthServiceImpl(PenggunaRepository penggunaRepository,
                           PasswordEncoder passwordEncoder,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper,
                           @Value("${tnde.login-url:http://192.168.1.131:1323/api/login}") String tndeLoginUrl) {
        this.penggunaRepository = penggunaRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tndeLoginUrl = tndeLoginUrl;
    }

    @Override
    public Pengguna getByUsername(String username) {
        return penggunaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan: " + username));
    }

    @Override
    @Transactional
    public TndeLoginResponseDto loginTnde(String username, String password) {
        TndeLoginRequestDto request = TndeLoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        TndeLoginResponseDto response;
        try {
            response = restTemplate.postForObject(tndeLoginUrl, request, TndeLoginResponseDto.class);
        } catch (ResourceAccessException ex) {
            throw new TndeLoginException("Server TNDE tidak dapat dihubungi, silakan coba lagi nanti", ex);
        } catch (HttpStatusCodeException ex) {
            TndeLoginResponseDto errorResponse = parseTndeErrorResponse(ex.getResponseBodyAsString());
            if (errorResponse != null && !errorResponse.isSuccess() && StringUtils.hasText(errorResponse.getMessage())) {
                throw new TndeLoginException(errorResponse.getMessage(), ex);
            }
            throw new TndeLoginException("Gagal memproses respons dari server TNDE", ex);
        } catch (RestClientException ex) {
            throw new TndeLoginException("Gagal memproses respons dari server TNDE", ex);
        }

        if (response == null) {
            throw new TndeLoginException("Server TNDE tidak dapat dihubungi, silakan coba lagi nanti");
        }
        if (!response.isSuccess()) {
            throw new TndeLoginException(response.getMessage());
        }
        if (response.getData() == null || response.getData().getUser() == null) {
            throw new TndeLoginException("Respons login TNDE tidak lengkap");
        }

        sinkronkanPengguna(response.getData().getUser());
        return response;
    }

    private TndeLoginResponseDto parseTndeErrorResponse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        try {
            return objectMapper.readValue(responseBody, TndeLoginResponseDto.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private void sinkronkanPengguna(TndeUserDataDto user) {
        Role role = mapLevelNameToRole(user.getLevelName());
        penggunaRepository.findByUsername(user.getUsername())
                .map(existing -> {
                    existing.setNama(user.getNama());
                    existing.setRole(role);
                    return penggunaRepository.save(existing);
                })
                .orElseGet(() -> penggunaRepository.save(Pengguna.builder()
                        .nama(user.getNama())
                        .username(user.getUsername())
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(role)
                        .aktif(true)
                        .build()));
    }

    private Role mapLevelNameToRole(String levelName) {
        if (levelName == null) {
            return Role.USER;
        }
        String normalized = levelName.trim().toLowerCase();
        if (normalized.contains("admin")) {
            return Role.ADMIN;
        }
        return Role.USER;
    }

    @Override
    @Transactional
    public PenggunaDto register(RegisterRequestDto request) {
        if (!request.getPassword().equals(request.getKonfirmasiPassword())) {
            throw new IllegalArgumentException("Konfirmasi password tidak cocok");
        }
        if (penggunaRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai, silakan pilih yang lain");
        }

        Pengguna pengguna = penggunaRepository.save(Pengguna.builder()
                .nama(request.getNama().trim())
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                // Pendaftaran mandiri SELALU role USER. Role tidak boleh datang dari
                // request — kalau tidak, siapa pun bisa mendaftar sebagai ADMIN.
                .role(Role.USER)
                // Menunggu persetujuan admin.
                .aktif(false)
                .build());

        return toDto(pengguna);
    }

    @Override
    @Transactional
    public void gantiPassword(GantiPasswordRequestDto request, String username) {
        if (!request.getPasswordBaru().equals(request.getKonfirmasiPasswordBaru())) {
            throw new IllegalArgumentException("Konfirmasi password baru tidak cocok");
        }

        Pengguna pengguna = getByUsername(username);

        // Sengaja IllegalArgumentException (-> 400), bukan BadCredentialsException:
        // BadCredentialsException adalah AuthenticationException, sehingga akan
        // tertangkap handler login dan dibalas 401 "Username atau password salah" —
        // menyesatkan, karena pengguna sudah terautentikasi dan hanya salah mengetik
        // password lamanya. Membocorkan "password lama salah" ke pemilik akun sendiri
        // bukan masalah keamanan.
        if (!passwordEncoder.matches(request.getPasswordLama(), pengguna.getPassword())) {
            throw new IllegalArgumentException("Password lama salah");
        }
        if (passwordEncoder.matches(request.getPasswordBaru(), pengguna.getPassword())) {
            throw new IllegalArgumentException("Password baru tidak boleh sama dengan password lama");
        }

        pengguna.setPassword(passwordEncoder.encode(request.getPasswordBaru()));
        penggunaRepository.save(pengguna);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenggunaDto> daftarMenungguAktivasi() {
        return penggunaRepository.findByAktifFalseOrderByIdDesc().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenggunaDto> daftarSemuaPengguna() {
        return penggunaRepository.findAllByOrderByIdDesc().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public PenggunaDto aktifkan(Long penggunaId) {
        Pengguna pengguna = penggunaRepository.findById(penggunaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan: " + penggunaId));
        pengguna.setAktif(true);
        return toDto(penggunaRepository.save(pengguna));
    }

    @Override
    @Transactional
    public PenggunaDto nonaktifkan(Long penggunaId, String usernamePelaku) {
        Pengguna pengguna = penggunaRepository.findById(penggunaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan: " + penggunaId));

        // Tanpa ini, admin terakhir bisa mengunci dirinya sendiri keluar dan tidak
        // ada lagi yang bisa mengaktifkan akun siapa pun.
        if (pengguna.getUsername().equals(usernamePelaku)) {
            throw new IllegalArgumentException("Anda tidak bisa menonaktifkan akun Anda sendiri");
        }
        if (pengguna.getRole() == Role.ADMIN
                && penggunaRepository.countByRoleAndAktifTrue(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Tidak bisa menonaktifkan admin aktif terakhir");
        }

        pengguna.setAktif(false);
        return toDto(penggunaRepository.save(pengguna));
    }

    private PenggunaDto toDto(Pengguna pengguna) {
        return PenggunaDto.builder()
                .id(pengguna.getId())
                .nama(pengguna.getNama())
                .username(pengguna.getUsername())
                .email(pengguna.getEmail())
                .role(pengguna.getRole().name())
                .aktif(pengguna.isAktif())
                .build();
    }
}
