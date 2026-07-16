import { restRequest } from '../api.js';

/**
 * Modul fitur pengajuan peminjaman alat (form-pinjam.html)
 */

export async function ajukanPeminjaman(payload) {
    // payload contoh: { alatId, tanggalPinjam, tanggalKembali, keperluan }
    return restRequest('/ajukan', {
        method: 'POST',
        body: JSON.stringify(payload)
    });
}

export async function getRiwayatPeminjaman() {
    return restRequest('/riwayat', {
        method: 'GET'
    });
}

export async function batalkanPeminjaman(peminjamanId) {
    return restRequest(`/${peminjamanId}/batal`, {
        method: 'PATCH'
    });
}
