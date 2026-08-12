package com.sitandi.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sitandi.model.Bidang;
import com.sitandi.model.Jabatan;
import com.sitandi.model.JabatanStruktur;
import com.sitandi.model.Opd;
import com.sitandi.model.TipeWilayah;
import com.sitandi.model.Wilayah;
import com.sitandi.repository.BidangRepository;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.WilayahRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

/**
 * Mengisi data master struktur organisasi (Wilayah, OPD, Bidang, Jabatan) dengan
 * data dummy skala kecil — KHUSUS untuk kebutuhan PRESENTASI/DEMO.
 *
 * <p>Skala data: 38 Kabupaten/Kota se-Jawa Timur tetap ada di tree (struktur
 * wilayahnya tidak berubah), tapi hanya 4 (empat) kabupaten/kota SAMPEL yang
 * diisi instansi lengkap (Surabaya, Malang, Sidoarjo, Kediri). Sisanya tetap
 * muncul sebagai node kosong di tree, supaya pembimbing melihat struktur 38
 * kab/kota tanpa harus generate ribuan baris data.</p>
 *
 * <p>Perkiraan total personal: 4 kab/kota × ~3 OPD × ~3 bidang × ~5 orang
 * ≈ 180 baris. Ringan untuk demo, mudah diganti nanti saat data produksi
 * tersedia.</p>
 *
 * <p>Idempotent: hanya berjalan jika tabel wilayah masih kosong.</p>
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class OrganisasiDataSeeder implements CommandLineRunner {

    private final WilayahRepository wilayahRepository;
    private final OpdRepository opdRepository;
    private final BidangRepository bidangRepository;
    private final JabatanStrukturRepository jabatanStrukturRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /** Daftar 38 Kabupaten/Kota di Jawa Timur dengan tipenya. */
    private static final List<KabKota> DAFTAR_KAB_KOTA = List.of(
            new KabKota("Kabupaten Bangkalan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Banyuwangi", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Blitar", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Bojonegoro", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Bondowoso", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Gresik", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Jember", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Jombang", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Kediri", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Lamongan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Lumajang", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Madiun", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Magetan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Malang", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Mojokerto", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Nganjuk", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Ngawi", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Pacitan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Pamekasan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Pasuruan", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Ponorogo", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Probolinggo", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Sampang", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Sidoarjo", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Situbondo", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Sumenep", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Trenggalek", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Tuban", TipeWilayah.KABUPATEN),
            new KabKota("Kabupaten Tulungagung", TipeWilayah.KABUPATEN),
            new KabKota("Kota Batu", TipeWilayah.KOTA),
            new KabKota("Kota Blitar", TipeWilayah.KOTA),
            new KabKota("Kota Kediri", TipeWilayah.KOTA),
            new KabKota("Kota Madiun", TipeWilayah.KOTA),
            new KabKota("Kota Malang", TipeWilayah.KOTA),
            new KabKota("Kota Mojokerto", TipeWilayah.KOTA),
            new KabKota("Kota Pasuruan", TipeWilayah.KOTA),
            new KabKota("Kota Probolinggo", TipeWilayah.KOTA),
            new KabKota("Kota Surabaya", TipeWilayah.KOTA)
    );

    /**
     * Daftar Instansi / OPD yang akan digenerate untuk SETIAP Kabupaten/Kota.
     */
    private static final List<String> DAFTAR_OPD = List.of(
            // Instansi besar (4-6 Bidang)
            "Dinas Komunikasi dan Informatika",
            "Dinas Pendidikan",
            "Dinas Kesehatan",
            "Dinas Pekerjaan Umum dan Penataan Ruang",
            // Instansi sedang (3-4 Bidang)
            "Dinas Sosial",
            "Dinas Lingkungan Hidup",
            "Dinas Pertanian",
            "Dinas Perindustrian dan Perdagangan",
            "Dinas Perhubungan",
            // Instansi kecil (2-3 Bidang)
            "Badan Kepegawaian Daerah",
            "Badan Pengelolaan Keuangan dan Aset Daerah",
            "Satuan Polisi Pamong Praja",
            "Inspektorat Daerah"
    );

    /** Daftar Bidang generik untuk setiap OPD contoh Kominfo. */
    private static final List<String> BIDANG_KOMINFO = List.of(
            "Sekretariat",
            "Bidang Infrastruktur Teknologi Informasi",
            "Bidang Layanan Aplikasi Informatika",
            "Bidang Komunikasi dan Informasi Publik",
            "Bidang Persandian dan Statistik"
    );

    /** Daftar Bidang generik untuk Dinas Pendidikan. */
    private static final List<String> BIDANG_PENDIDIKAN = List.of(
            "Sekretariat",
            "Bidang Pembinaan PAUD dan Pendidikan Non Formal",
            "Bidang Pembinaan Sekolah Dasar",
            "Bidang Pembinaan Sekolah Menengah Pertama",
            "Bidang Pembinaan Ketenagaan"
    );

    /** Daftar Bidang generik untuk Dinas Kesehatan. */
    private static final List<String> BIDANG_KESEHATAN = List.of(
            "Sekretariat",
            "Bidang Kesehatan Masyarakat",
            "Bidang Pencegahan dan Pengendalian Penyakit",
            "Bidang Pelayanan Kesehatan",
            "Bidang Sumber Daya Kesehatan"
    );

    /** Daftar Bidang generik untuk DPUPR. */
    private static final List<String> BIDANG_PUPR = List.of(
            "Sekretariat",
            "Bidang Bina Marga",
            "Bidang Sumber Daya Air",
            "Bidang Cipta Karya",
            "Bidang Penataan Ruang"
    );

    /** Daftar Bidang generik untuk Dinas Perhubungan. */
    private static final List<String> BIDANG_PERHUBUNGAN = List.of(
            "Sekretariat",
            "Bidang Lalu Lintas",
            "Bidang Angkutan",
            "Bidang Sarana dan Prasarana"
    );

    private static final List<String> BIDANG_SOSIAL = List.of(
            "Sekretariat", 
            "Bidang Perlindungan dan Jaminan Sosial", 
            "Bidang Rehabilitasi Sosial", 
            "Bidang Pemberdayaan Sosial"
    );
    private static final List<String> BIDANG_DLH = List.of(
            "Sekretariat", 
            "Bidang Tata Lingkungan", 
            "Bidang Pengelolaan Sampah dan B3", 
            "Bidang Pengendalian Pencemaran"
    );
    private static final List<String> BIDANG_PERTANIAN = List.of(
            "Sekretariat", 
            "Bidang Tanaman Pangan", 
            "Bidang Hortikultura", 
            "Bidang Peternakan"
    );
    private static final List<String> BIDANG_PERINDAG = List.of(
            "Sekretariat", 
            "Bidang Perindustrian", 
            "Bidang Perdagangan Dalam Negeri", 
            "Bidang Metrologi Legal"
    );
    private static final List<String> BIDANG_BKD = List.of(
            "Sekretariat", 
            "Bidang Mutasi dan Promosi", 
            "Bidang Pengembangan Aparatur"
    );
    private static final List<String> BIDANG_BPKAD = List.of(
            "Sekretariat", 
            "Bidang Anggaran", 
            "Bidang Perbendaharaan", 
            "Bidang Aset"
    );
    private static final List<String> BIDANG_SATPOL = List.of(
            "Sekretariat", 
            "Bidang Penegakan Perda", 
            "Bidang Ketertiban Umum"
    );
    private static final List<String> BIDANG_INSPEKTORAT = List.of(
            "Sekretariat", 
            "Inspektur Pembantu I", 
            "Inspektur Pembantu II"
    );

    /** Struktur jabatan standar untuk tiap Bidang (5 orang: Ketua, Sekretaris, Bendahara, 2 Anggota). */
    private static final List<Jabatan> STRUKTUR_JABATAN = List.of(
            Jabatan.KETUA, Jabatan.SEKRETARIS, Jabatan.BENDAHARA,
            Jabatan.ANGGOTA, Jabatan.ANGGOTA
    );

    /** Counter deterministik untuk pengulangan nama dummy. */
    private int namaCounter = 0;

    /** Counter username untuk suffix urut saat terjadi duplikat. */
    private final java.util.Map<String, Integer> usernameCounter = new java.util.HashMap<>();

    private static final String[] NAMA_POOL = {
            "Ahmad Fauzi", "Budi Hartono", "Citra Dewi Lestari",
            "Deni Prasetyo", "Eka Fitriani", "Fajar Setiawan",
            "Gita Permata Sari", "Hendra Kusuma", "Indah Permatasari",
            "Joko Susilo", "Kartika Sari Dewi", "Luki Ardiansyah",
            "Mega Puspita Rini", "Nugroho Adi Wibowo", "Olivia Putri Maharani",
            "Prasetyo Budi Utomo", "Ratna Dewi Kusumawati", "Rizky Fadhilah",
            "Sari Dewi Anggraini", "Taufik Hidayat", "Umi Kalsum",
            "Vina Aulia Rahmah", "Wahyu Nugroho", "Yuni Astuti",
            "Bambang Wijaya", "Siti Nurhayati", "Agus Supriyanto",
            "Dewi Sartika", "Mochammad Rizal", "Abdul Malik",
            "Sutrisno", "Endang Purwanti", "Sunaryo",
            "Rini Ambarwati", "Hendra Gunawan", "Yuliana Dewi",
            "Arief Rachman", "Fitri Handayani", "Eko Prasetyo",
            "Nina Marlina", "Doni Kusuma", "Ratna Puspita",
            "Adi Saputra", "Tuti Alawiyah", "Agus Salim",
            "Lilis Suryani", "Diana Permata Sari", "Haryanto",
            "Ani Rahmawati", "Slamet Riyadi"
    };

    @Override
    @Transactional
    public void run(String... args) {
        long wilayahCount = wilayahRepository.count();
        long opdCount = opdRepository.count();
        long jabatanCount = jabatanStrukturRepository.count();
        System.out.println("[OrganisasiDataSeeder] wilayah=" + wilayahCount + " opd=" + opdCount
                + " jabatan=" + jabatanCount);

        if (wilayahCount > 0) {
            /*
             * Wilayah sudah ada. Cek kondisi:
             * - legacySeed=true: ada OPD dari versi seed lama (semua 38 kab/kota punya OPD)
             * - ada wilayah tapi OPD kosong: tabel baru di-truncate / seeder gagal sebelumnya
             *   (reseed dengan skala kecil)
             * - ada jabatan tapi belum punya username: kolom baru ditambahkan oleh ddl-auto=update
             *   dengan nilai NULL (reseed biar DTO barisnya terisi)
             * - wilayah + OPD sudah ada dari seed baru: skip
             */
            boolean legacySeed = opdRepository.findAll().stream()
                    .anyMatch(o -> o.getNamaOpd() != null
                            && o.getNamaOpd().contains("Kabupaten Bangkalan"));
            boolean orphanedWilayah = (wilayahCount > 0 && opdCount == 0);
            boolean legacyMissingUsername = jabatanStrukturRepository.findAll().stream()
                    .anyMatch(js -> js.getUsername() == null || js.getUsername().isBlank());
            boolean legacyMissingNip = jabatanStrukturRepository.findAll().stream()
                    .anyMatch(js -> js.getNip() == null || js.getNip().isBlank());
            long expectedOpdCount = (long) DAFTAR_KAB_KOTA.size() * DAFTAR_OPD.size();
            boolean scaleChanged = (opdCount != expectedOpdCount && opdCount > 0);
            
            System.out.println("[OrganisasiDataSeeder] legacySeed=" + legacySeed
                    + " orphanedWilayah=" + orphanedWilayah
                    + " legacyMissingUsername=" + legacyMissingUsername
                    + " legacyMissingNip=" + legacyMissingNip
                    + " scaleChanged=" + scaleChanged);

            if (legacySeed || orphanedWilayah || legacyMissingUsername || legacyMissingNip || scaleChanged) {
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE jabatan_struktur").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE bidang").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE opd").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE wilayah").executeUpdate();
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
                entityManager.clear();
            } else {
                return;
            }
        }

        Wilayah provinsi = wilayahRepository.save(Wilayah.builder()
                .nama("Jawa Timur")
                .tipe(TipeWilayah.PROVINSI)
                .parent(null)
                .build());

        int totalOpd = 0, totalBidang = 0, totalJabatan = 0;
        for (KabKota kk : DAFTAR_KAB_KOTA) {
            Wilayah wilayah = wilayahRepository.save(Wilayah.builder()
                    .nama(kk.nama())
                    .tipe(kk.tipe())
                    .parent(provinsi)
                    .build());

            int opdCounter = 1;
            for (String namaOpd : DAFTAR_OPD) {
                Opd opd = opdRepository.save(Opd.builder()
                        .wilayah(wilayah)
                        .namaOpd(namaOpd + " " + kk.nama())
                        .kodeOpd(kodeWilayah(kk.nama()) + "-OPD-" + String.format("%02d", opdCounter++))
                        .build());
                totalOpd++;

                // Pilih daftar bidang sesuai tipe OPD
                List<String> daftarBidang = pilihBidang(namaOpd);

                // 4. Buat Bidang per OPD
                for (String namaBidang : daftarBidang) {
                    Bidang bidang = bidangRepository.save(Bidang.builder()
                            .opd(opd)
                            .namaBidang(namaBidang)
                            .build());
                    totalBidang++;

                    // 5. Buat jabatan standar: Ketua, Sekretaris, Bendahara, 2 Anggota
                    for (Jabatan jabatan : STRUKTUR_JABATAN) {
                        String nama = nextNama();
                        String username = nextUsername(nama);
                        jabatanStrukturRepository.save(JabatanStruktur.builder()
                                .bidang(bidang)
                                .jabatan(jabatan)
                                .namaDummy(nama)
                                .username(username)
                                .nip(nextNip())
                                .email(nextEmail(username, opd.getNamaOpd()))
                                .noHp(nextNoHp())
                                .build());
                        totalJabatan++;
                    }
                }
            }
        }
        System.out.println("[OrganisasiDataSeeder] Seeded: " + totalOpd + " OPD, "
                + totalBidang + " Bidang, " + totalJabatan + " Jabatan");
    }

    /** Pilih daftar bidang berdasarkan prefiks nama OPD. */
    private List<String> pilihBidang(String namaOpd) {
        String lower = namaOpd.toLowerCase();
        if (lower.contains("komunikasi") || lower.contains("informatika")) return BIDANG_KOMINFO;
        if (lower.contains("pendidikan")) return BIDANG_PENDIDIKAN;
        if (lower.contains("kesehatan")) return BIDANG_KESEHATAN;
        if (lower.contains("pekerjaan umum") || lower.contains("pupr")) return BIDANG_PUPR;
        if (lower.contains("perhubungan")) return BIDANG_PERHUBUNGAN;
        
        if (lower.contains("sosial")) return BIDANG_SOSIAL;
        if (lower.contains("lingkungan hidup")) return BIDANG_DLH;
        if (lower.contains("pertanian")) return BIDANG_PERTANIAN;
        if (lower.contains("perindustrian") || lower.contains("perdagangan")) return BIDANG_PERINDAG;
        
        if (lower.contains("kepegawaian")) return BIDANG_BKD;
        if (lower.contains("keuangan") || lower.contains("aset")) return BIDANG_BPKAD;
        if (lower.contains("pamong praja") || lower.contains("satpol")) return BIDANG_SATPOL;
        if (lower.contains("inspektorat")) return BIDANG_INSPEKTORAT;

        // Default fallback
        return List.of("Sekretariat", "Bidang Umum");
    }

    /** Nama dummy deterministik dari pool, diulang dengan indeks. */
    private String nextNama() {
        String nama = NAMA_POOL[namaCounter % NAMA_POOL.length];
        namaCounter++;
        return nama;
    }

    /**
     * Generate username dummy dari nama lengkap: inisial depan (1 huruf) +
     * nama keluarga (token terakhir), lowercase, tanpa spasi. Tangani duplikat
     * dengan suffix angka urut (afauzi, afauzi2, afauzi3, ...). Kolom username
     * disimpan di jabatan_struktur, bukan dipakai untuk autentikasi.
     */
    private String nextUsername(String namaLengkap) {
        String[] tokens = namaLengkap.trim().toLowerCase().split("\\s+");
        String inisialDepan = tokens[0].substring(0, 1);
        String namaBelakang = tokens[tokens.length - 1].replaceAll("[^a-z0-9]", "");
        String base = inisialDepan + namaBelakang;
        int seen = usernameCounter.getOrDefault(base, 0);
        usernameCounter.put(base, seen + 1);
        return seen == 0 ? base : base + (seen + 1);
    }

    /** Generate NIP dummy 18 digit (format: tanggal lahir dummy + urutan). */
    private String nextNip() {
        int tahun = 1970 + (namaCounter % 20);
        int bulan = (namaCounter % 12) + 1;
        int tgl = (namaCounter % 28) + 1;
        int urutan = namaCounter % 1000;
        return String.format("%04d%02d%02d%06d%03d", tahun, bulan, tgl, 201001, urutan);
    }

    /** Generate email dummy format username@slug-opd.go.id */
    private String nextEmail(String username, String namaOpd) {
        String slug = namaOpd.toLowerCase()
                .replaceAll("kota ", "")
                .replaceAll("kabupaten ", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return username + "@" + slug + ".go.id";
    }

    /** Generate no HP dummy format 08xx-xxxx-xxxx */
    private String nextNoHp() {
        int prefix = 11 + (namaCounter % 9); // 0811 - 0819
        int mid = 1000 + (namaCounter % 9000);
        int suffix = 1000 + ((namaCounter * 3) % 9000);
        return String.format("08%d-%04d-%04d", prefix, mid, suffix);
    }

    /** Bikin kode unik dari nama kab/kota untuk prefix kodeOpd yang unik secara global. */
    private String kodeWilayah(String namaKabKota) {
        return namaKabKota.replace("Kabupaten ", "KAB_").replace("Kota ", "KOTA_").replace(" ", "").toUpperCase();
    }

    /** Record sederhana untuk pasangan nama + tipe kab/kota (seluruh daftar 38). */
    private record KabKota(String nama, TipeWilayah tipe) {}
}