import { restRequest } from '../api.js';

/**
 * Modul fitur pengembalian alat setelah masa pinjam selesai
 */

export async function konfirmasiPengembalian(peminjamanId, payload) {
    // payload contoh: { kondisiSaatKembali, catatan }
    return restRequest(`/${peminjamanId}/kembalikan`, {
        method: 'PATCH',
        body: JSON.stringify(payload)
    });
}

export async function getStatusPengembalian(peminjamanId) {
    return restRequest(`/${peminjamanId}/status-pengembalian`, {
        method: 'GET'
    });
}
