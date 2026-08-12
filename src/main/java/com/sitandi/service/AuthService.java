package com.sitandi.service;

import java.util.List;

import com.sitandi.dto.GantiPasswordRequestDto;
import com.sitandi.dto.PenggunaDto;
import com.sitandi.dto.RegisterRequestDto;
import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.model.Pengguna;

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

    /**
     * Login menggunakan API eksternal TNDE. Memanggil endpoint TNDE, lalu
     * menyinkronkan data pengguna lokal yang diperlukan untuk otorisasi.
     *
     * @throws com.sitandi.exception.TndeLoginException bila login TNDE gagal
     */
    TndeLoginResponseDto loginTnde(String username, String password);

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

    /**
     * Hapus permanen akun pengguna. Admin tidak bisa menghapus akunnya sendiri.
     */
    void hapusPengguna(Long penggunaId, String usernamePelaku);
}
