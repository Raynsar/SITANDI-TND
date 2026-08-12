

document.addEventListener('DOMContentLoaded', () => {
    console.log('SITANDI Frontend Core Layer Engine initialized.');

    // Tombol "Masuk Akun" di landing page -> halaman login
    const loginBtn = document.getElementById('btn-login');
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            window.location.href = (window.APP_CONTEXT_PATH || '/') + 'login';
        });
    }

    // --- Dark Mode Logic ---
    const htmlEl = document.documentElement;
    const STORAGE_KEY = 'sitandi-theme';

    function getPreferredTheme() {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === 'dark' || stored === 'light') {
            return stored;
        }
        return 'light'; // Default
    }

    function setTheme(theme) {
        htmlEl.setAttribute('data-bs-theme', theme);
        localStorage.setItem(STORAGE_KEY, theme);
        
        const icons = document.querySelectorAll('#darkModeIcon');
        icons.forEach(icon => {
            if (theme === 'dark') {
                icon.className = 'bi bi-sun-fill';
            } else {
                icon.className = 'bi bi-moon-fill';
            }
        });
    }

    // Apply saved theme immediately on load
    setTheme(getPreferredTheme());

    // Event Delegation untuk tombol toggle dark mode
    // Karena tombol ini ada di header, kita bisa pakai delegasi jika sewaktu-waktu elemen di-replace
    document.addEventListener('click', (e) => {
        // Cek jika yang diklik adalah tombol toggle atau icon di dalamnya
        const toggleBtn = e.target.closest('#darkModeToggle');
        if (toggleBtn) {
            const current = htmlEl.getAttribute('data-bs-theme') || 'light';
            const next = current === 'dark' ? 'light' : 'dark';
            setTheme(next);
        }
    });
});
