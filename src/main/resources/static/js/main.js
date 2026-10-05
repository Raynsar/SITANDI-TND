

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

    // --- Global Search (navbar) ---
    const searchInput = document.getElementById('globalSearchInput');
    const searchResults = document.getElementById('globalSearchResults');

    if (searchInput && searchResults) {
        let debounceTimer = null;

        const tutupDropdown = () => {
            searchResults.classList.add('d-none');
            searchResults.classList.remove('show');
        };

        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            const q = searchInput.value.trim();
            if (q.length < 2) {
                tutupDropdown();
                return;
            }
            debounceTimer = setTimeout(() => {
                fetch((window.APP_CONTEXT_PATH || '/') + 'api/v1/search?q=' + encodeURIComponent(q), {
                    method: 'GET',
                    headers: { 'Accept': 'application/json' },
                    credentials: 'same-origin'
                })
                    .then(res => res.ok ? res.json() : Promise.reject(res.status))
                    .then(data => renderHasil(data))
                    .catch(() => tutupDropdown());
            }, 300); // debounce ~300ms
        });

        document.addEventListener('click', (e) => {
            if (!e.target.closest('#globalSearchInput') && !e.target.closest('#globalSearchResults')) {
                tutupDropdown();
            }
        });

        function renderHasil(data) {
            let html = '';

            const renderItem = (label, sub, url) =>
                `<a class="dropdown-item text-wrap" href="${url}">
                    <span class="fw-semibold">${label}</span>
                    <br><small class="text-body-secondary">${sub}</small>
                 </a>`;

            if (data.personal && data.personal.length) {
                html += `<h6 class="dropdown-header"><i class="bi bi-person me-1"></i>Personal</h6>`;
                data.personal.forEach(p => {
                    html += renderItem(
                        escapeHtml(p.nama),
                        escapeHtml((p.jabatan || '-') + ' • ' + (p.nip || '-')),
                        (window.APP_CONTEXT_PATH || '/') + 'organisasi/tree?bidangId=' + p.bidangId
                    );
                });
            }
            if (data.opd && data.opd.length) {
                html += `<h6 class="dropdown-header"><i class="bi bi-building me-1"></i>OPD</h6>`;
                data.opd.forEach(o => {
                    html += renderItem(escapeHtml(o.nama), 'OPD',
                        (window.APP_CONTEXT_PATH || '/') + 'organisasi/tree?opdId=' + o.id);
                });
            }
            if (data.user && data.user.length) {
                html += `<h6 class="dropdown-header"><i class="bi bi-people me-1"></i>Pengguna</h6>`;
                data.user.forEach(u => {
                    html += renderItem(escapeHtml(u.nama),
                        escapeHtml(u.username + ' • ' + u.role),
                        (window.APP_CONTEXT_PATH || '/') + 'admin/pengguna');
                });
            }
            if (!html) {
                html = `<span class="dropdown-item-text text-body-secondary">Tidak ada hasil.</span>`;
            }
            searchResults.innerHTML = html;
            searchResults.classList.remove('d-none');
            searchResults.classList.add('show');
        }

        function escapeHtml(s) {
            return String(s == null ? '' : s)
                .replace(/&/g, '&amp;').replace(/</g, '&lt;')
                .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        }
    }

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
