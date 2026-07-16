package com.sipenjam.peminjamanalattu.service;

import java.util.List;

import com.sipenjam.peminjamanalattu.dto.GantiPasswordRequestDto;
import com.sipenjam.peminjamanalattu.dto.PenggunaDto;
import com.sipenjam.peminjamanalattu.dto.RegisterRequestDto;
import com.sipenjam.peminjamanalattu.model.Pengguna;

/**
 * Layanan otentikasi & pengelolaan akun pengguna.
 */
public interface AuthService {

    /** Ambil entity pengguna berdasarkan username (dipakai untuk cek ownership). */
    Pengguna getByUsername(String username);

    /**
     * Daftar akun baru secara mandiri. Akun dibuat dengan role USER dan
     * aktif=false — harus disetujui admin sebelum bisa login.
     *
     * @throws IllegalArgumentException bila username sudah dipakai atau konfirmasi
     *                                  password tidak cocok
     */
    PenggunaDto register(RegisterRequestDto request);

    /**
     * Ganti password milik sendiri. Wajib menyertakan password lama yang benar.
     *
     * @throws IllegalArgumentException bila password lama salah, konfirmasi tidak
     *                                  cocok, atau password baru sama dengan yang lama
     */
    void gantiPassword(GantiPasswordRequestDto request, String username);

    /** Daftar akun yang menunggu persetujuan admin (aktif=false). */
    List<PenggunaDto> daftarMenungguAktivasi();

    /** Seluruh akun, untuk halaman kelola pengguna admin. */
    List<PenggunaDto> daftarSemuaPengguna();

    /** Setujui akun sehingga pemiliknya bisa login. */
    PenggunaDto aktifkan(Long penggunaId);

    /**
     * Nonaktifkan akun. Pemiliknya langsung tidak bisa login lagi, tapi token JWT
     * yang sudah terlanjur terbit tetap ditolak karena status aktif dicek ulang
     * dari database di setiap request.
     */
    PenggunaDto nonaktifkan(Long penggunaId, String usernamePelaku);
}
