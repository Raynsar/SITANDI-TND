package com.sitandi.service;

import java.util.List;

import com.sitandi.dto.SuratMasukResponseDto.SuratDto;

/**
 * Layanan untuk mengambil data surat masuk / inbox dari API eksternal TNDE.
 */
public interface SuratMasukService {

    /**
     * Ambil daftar surat masuk dari TNDE untuk tahun tertentu.
     *
     * @param tndeToken token JWT dari session hasil login TNDE
     * @param tahun     tahun filter surat (default tahun berjalan)
     * @return daftar surat masuk
     * @throws com.sitandi.exception.TndeServerException
     *             bila server TNDE tidak bisa dihubungi
     * @throws com.sitandi.exception.TndeLoginException
     *             bila token TNDE tidak valid / expired
     */
    List<SuratDto> ambilSuratMasuk(String tndeToken, int tahun);
}

