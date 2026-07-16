package com.sipenjam.peminjamanalattu.service.impl;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sipenjam.peminjamanalattu.dto.GantiPasswordRequestDto;
import com.sipenjam.peminjamanalattu.dto.PenggunaDto;
import com.sipenjam.peminjamanalattu.dto.RegisterRequestDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.repository.PenggunaRepository;
import com.sipenjam.peminjamanalattu.service.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * Implementasi otentikasi & pengelolaan akun. Sekaligus menjadi
 * {@link UserDetailsService} untuk Spring Security, memetakan Role ke ROLE_*.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final PenggunaRepository penggunaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Status aktif SENGAJA tidak dipetakan ke {@code User.disabled()}.
     *
     * DaoAuthenticationProvider menjalankan pemeriksaan status akun SEBELUM
     * mencocokkan password. Kalau akun nonaktif ditandai disabled di sini, maka
     * mengirim username yang benar dengan password ngawur tetap menghasilkan
     * DisabledException — dan pesan "akun menunggu persetujuan" itu membocorkan
     * bahwa username tersebut terdaftar, tanpa penyerang perlu tahu passwordnya
     * (user enumeration).
     *
     * Karena itu status aktif dicek SETELAH password terbukti benar, yaitu di
     * JwtLoginSuccessHandler (form login) dan AuthApiController (login API), serta
     * di setiap request oleh JwtAuthenticationFilter.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Pengguna pengguna = penggunaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan: " + username));

        return new User(
                pengguna.getUsername(),
                pengguna.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + pengguna.getRole().name()))
        );
    }

    @Override
    public Pengguna getByUsername(String username) {
        return penggunaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan: " + username));
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
