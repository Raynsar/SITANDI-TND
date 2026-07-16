import { restRequest } from '../api.js';

/**
 * Modul fitur katalog & inventaris alat (katalog.html, admin/inventory.html)
 */

export async function getKatalogAlat() {
    return restRequest('/inventaris', {
        method: 'GET'
    });
}

export async function getDetailAlat(alatId) {
    return restRequest(`/inventaris/${alatId}`, {
        method: 'GET'
    });
}

// Khusus admin: update stok/kondisi alat
export async function updateStokAlat(alatId, payload) {
    // payload contoh: { stok, kondisi }
    return restRequest(`/inventaris/${alatId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
    });
}
