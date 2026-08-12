package com.sipenjam.peminjamanalattu.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sipenjam.peminjamanalattu.model.Bidang;
import com.sipenjam.peminjamanalattu.model.Jabatan;
import com.sipenjam.peminjamanalattu.model.JabatanStruktur;
import com.sipenjam.peminjamanalattu.model.Opd;
import com.sipenjam.peminjamanalattu.model.TipeWilayah;
import com.sipenjam.peminjamanalattu.model.Wilayah;
import com.sipenjam.peminjamanalattu.repository.BidangRepository;
import com.sipenjam.peminjamanalattu.repository.JabatanStrukturRepository;
import com.sipenjam.peminjamanalattu.repository.OpdRepository;
import com.sipenjam.peminjamanalattu.repository.WilayahRepository;

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
     * Kab/kota SAMPEL yang akan diisi lengkap dengan OPD + Bidang + Jabatan.
     * Kab/kota lain tetap muncul di tree tapi tanpa OPD di bawahnya (kosong).
     */
    private static final List<SampleKabKota> SAMPEL_LENGKAP = List.of(
            new SampleKabKota("Kota Surabaya", List.of(
                    "Dinas Komunikasi dan Informatika",
                    "Dinas Pendidikan",
                    "Dinas Kesehatan"
            )),
            new SampleKabKota("Kabupaten Malang", List.of(
                    "Dinas Komunikasi dan Informatika",
                    "Dinas Pekerjaan Umum dan Penataan Ruang"
            )),
            new SampleKabKota("Kabupaten Sidoarjo", List.of(
                    "Dinas Komunikasi dan Informatika"
            )),
            new SampleKabKota("Kota Kediri", List.of(
                    "Dinas Komunikasi dan Informatika",
                    "Dinas Perhubungan"
            ))
    );

    /** Lookup cepat nama → tipe kab/kota untuk sampel. */
    private static final java.util.Map<String, List<String>> SAMPEL_BY_NAMA = new java.util.HashMap<>();
    static {
        for (SampleKabKota s : SAMPEL_LENGKAP) {
            SAMPEL_BY_NAMA.put(s.nama(), s.opdList());
        }
    }

    /** Daftar Bidang generik untuk setiap OPD contoh Kominfo. */
    private static final List<String> BIDANG_KOMINFO = List.of(
            "Sekretariat",
            "Bidang Infrastruktur Teknologi Informasi",
            "Bidang Layanan Aplikasi Informatika",
            "Bidang Komunikasi dan Informasi Publik"
    );

    /** Daftar Bidang generik untuk Dinas Pendidikan. */
    private static final List<String> BIDANG_PENDIDIKAN = List.of(
            "Sekretariat",
            "Bidang Pembinaan PAUD dan Pendidikan Non Formal",
            "Bidang Pembinaan Sekolah Dasar"
    );

    /** Daftar Bidang generik untuk Dinas Kesehatan. */
    private static final List<String> BIDANG_KESEHATAN = List.of(
            "Sekretariat",
            "Bidang Pelayanan Kesehatan",
            "Bidang Pencegahan dan Pengendalian Penyakit"
    );

    /** Daftar Bidang generik untuk DPUPR. */
    private static final List<String> BIDANG_PUPR = List.of(
            "Sekretariat",
            "Bidang Bina Marga",
            "Bidang Cipta Karya"
    );

    /** Daftar Bidang generik untuk Dinas Perhubungan. */
    private static final List<String> BIDANG_PERHUBUNGAN = List.of(
            "Sekretariat",
            "Bidang Lalu Lintas",
            "Bidang Angkutan"
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
            // Wilayah sudah ada. Cek kondisi:
            // - legacySeed=true: ada OPD dari versi seed lama (semua 38 kab/kota punya OPD)
            // - ada wilayah tapi OPD kosong: tabel baru di-truncate / seeder gagal
            //   sebelumnya — reseed dengan skala kecil
            // - ada jabatan tapi belum punya username: kolom baru ditambahkan oleh
            //   ddl-auto=update dengan nilai NULL — reseed biar DTO baris-nya terisi
            // - wilayah + OPD sudah ada dari seed baru: skip
            boolean legacySeed = opdRepository.findAll().stream()
                    .anyMatch(o -> o.getNamaOpd() != null
                            && o.getNamaOpd().contains("Kabupaten Bangkalan"));
            boolean orphanedWilayah = (wilayahCount > 0 && opdCount == 0);
            boolean legacyMissingUsername = jabatanStrukturRepository.findAll().stream()
                    .anyMatch(js -> js.getUsername() == null || js.getUsername().isBlank());
            System.out.println("[OrganisasiDataSeeder] legacySeed=" + legacySeed
                    + " orphanedWilayah=" + orphanedWilayah
                    + " legacyMissingUsername=" + legacyMissingUsername);

            if (legacySeed || orphanedWilayah || legacyMissingUsername) {
                // Reset agar bisa di-seed ulang dengan skala kecil.
                // Hanya jalan di environment demo.
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE jabatan_struktur").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE bidang").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE opd").executeUpdate();
                entityManager.createNativeQuery("TRUNCATE TABLE wilayah").executeUpdate();
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
                // Penting: clear persistence context setelah truncate. Sebelumnya kita
                // sudah load all() di legacyMissingUsername check, sehingga Hibernate
                // masih memegang entity JabatanStruktur id=N di session cache. Tanpa
                // clear(), save() berikutnya akan melempar NonUniqueObjectException
                // karena Hibernate mengira id itu sudah dipakai object lain.
                entityManager.clear();
            } else {
                return; // sudah pakai seed baru, jangan duplikasi
            }
        }

        // 1. Buat root Provinsi Jawa Timur
        Wilayah provinsi = wilayahRepository.save(Wilayah.builder()
                .nama("Jawa Timur")
                .tipe(TipeWilayah.PROVINSI)
                .parent(null)
                .build());

        // 2. Buat 38 Kabupaten/Kota
        int totalOpd = 0, totalBidang = 0, totalJabatan = 0;
        for (KabKota kk : DAFTAR_KAB_KOTA) {
            Wilayah wilayah = wilayahRepository.save(Wilayah.builder()
                    .nama(kk.nama())
                    .tipe(kk.tipe())
                    .parent(provinsi)
                    .build());

            // Cek apakah kabupaten/kota ini ada di daftar sampel
            List<String> opdList = SAMPEL_BY_NAMA.get(kk.nama());
            if (opdList == null) {
                // Bukan sampel — node tetap ada tapi kosong (tidak ada OPD/Bidang)
                continue;
            }

            // 3. Buat OPD untuk sampel
            int opdCounter = 1;
            for (String namaOpd : opdList) {
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
                        jabatanStrukturRepository.save(JabatanStruktur.builder()
                                .bidang(bidang)
                                .jabatan(jabatan)
                                .namaDummy(nama)
                                .username(nextUsername(nama))
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

    /** Bikin kode singkat dari nama kab/kota untuk prefix kodeOpd yang unik secara global. */
    private String kodeWilayah(String namaKabKota) {
        String bersih = namaKabKota.replace("Kabupaten ", "").replace("Kota ", "");
        return bersih.length() >= 3
                ? bersih.substring(0, 3).toUpperCase()
                : bersih.toUpperCase();
    }

    /** Record sederhana untuk pasangan nama + tipe kab/kota (seluruh daftar 38). */
    private record KabKota(String nama, TipeWilayah tipe) {}

    /** Record untuk sampel lengkap: nama kab/kota + daftar OPD di dalamnya. */
    private record SampleKabKota(String nama, List<String> opdList) {}
}