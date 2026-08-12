import { restRequest } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    console.log('SITANDI Frontend Core Layer Engine initialized.');

    // Tombol "Masuk Akun" di landing page -> halaman login
    const loginBtn = document.getElementById('btn-login');
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            window.location.href = (window.APP_CONTEXT_PATH || '/') + 'login';
        });
    }
});
