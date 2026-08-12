/**
 * API Bridge Service untuk integrasi asinkronus ke REST Controller Spring Boot
 *
 * APP_CONTEXT_PATH diinject Thymeleaf lewat fragment common/context-path.html
 * dan selalu berakhiran '/' ('/' saat lokal, '/sitandi/' saat deploy WAR).
 * window.location.origin TIDAK dipakai karena tidak menyertakan context path.
 *
 * Autentikasi TIDAK diurus di sini: JWT dibawa cookie http-only SITANDI_TOKEN yang
 * dikirim browser otomatis. Token sengaja tidak disimpan di sessionStorage/localStorage
 * supaya tidak bisa dibaca script bila terjadi XSS.
 *
 * Yang harus diurus di sini justru CSRF: karena cookie ikut terkirim otomatis, setiap
 * request yang mengubah data wajib membawa token CSRF sebagai bukti bahwa request
 * benar-benar berasal dari halaman kita. Server menaruh tokennya di cookie XSRF-TOKEN
 * (sengaja tidak http-only agar terbaca di sini) dan menantikannya kembali di header
 * X-XSRF-TOKEN.
 */
const AUTH_URL = (window.APP_CONTEXT_PATH || '/') + 'api/v1/auth';

/** Method yang tidak mengubah state tidak diperiksa CSRF oleh Spring Security. */
const METHOD_AMAN = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

function bacaCookie(nama) {
    return document.cookie
        .split('; ')
        .find((baris) => baris.startsWith(nama + '='))
        ?.split('=')
        .slice(1)
        .join('=');
}

async function kirim(baseUrl, endpoint, options = {}) {
    const method = (options.method || 'GET').toUpperCase();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (!METHOD_AMAN.includes(method)) {
        const csrf = bacaCookie('XSRF-TOKEN');
        if (csrf) {
            headers['X-XSRF-TOKEN'] = decodeURIComponent(csrf);
        }
    }

    const response = await fetch(`${baseUrl}${endpoint}`, {
        ...options,
        method,
        headers,
        // Cookie JWT & CSRF wajib ikut. Default 'same-origin' sebenarnya sudah cukup,
        // tapi ditulis eksplisit agar niatnya jelas.
        credentials: 'same-origin'
    });

    if (response.status === 401) {
        // Token habis masa berlaku atau akun dinonaktifkan. Ini navigasi browser biasa,
        // jadi arahkan ke halaman login, bukan melempar error yang tidak bisa ditindak.
        window.location.href = (window.APP_CONTEXT_PATH || '/') + 'login';
        // Promise yang tidak pernah selesai: mencegah pemanggil menampilkan pesan error
        // sekejap sebelum halaman berpindah.
        return new Promise(() => {});
    }

    if (!response.ok) {
        throw new Error(await pesanError(response));
    }

    if (response.status === 204) return null;
    return response.json();
}

/**
 * Mengambil pesan dari body JSON GlobalExceptionHandler bila ada, supaya pengguna
 * melihat "Username sudah dipakai" alih-alih "HTTP 400 Bad Request".
 */
async function pesanError(response) {
    try {
        const body = await response.json();
        if (body.errors) {
            const detail = Object.values(body.errors).join(', ');
            if (detail) return detail;
        }
        if (body.message) return body.message;
    } catch (ignored) {
        // Body bukan JSON (mis. halaman error Tomcat) — pakai pesan default.
    }
    return `Kesalahan Endpoint HTTP: ${response.status} - ${response.statusText}`;
}

/** Request ke /api/v1/auth/** (register, ganti password, kelola akun). */
export async function authRequest(endpoint, options = {}) {
    return kirim(AUTH_URL, endpoint, options);
}
