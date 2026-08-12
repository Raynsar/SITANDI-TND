package com.sipenjam.peminjamanalattu.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

import lombok.RequiredArgsConstructor;

/**
 * Mengisi data master struktur organisasi (Wilayah, OPD, Bidang, Jabatan) dengan
 * data dummy 38 Kabupaten/Kota se-Jawa Timur.
 *
 * Idempotent: hanya berjalan jika tabel wilayah masih kosong.
 * Di-order setelah DataSeeder (Order(2)) agar berjalan setelah seeder utama.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class OrganisasiDataSeeder implements CommandLineRunner {

    private final WilayahRepository wilayahRepository;
    private final OpdRepository opdRepository;
    private final BidangRepository bidangRepository;
    private final JabatanStrukturRepository jabatanStrukturRepository;

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

    /** 7 Bidang generik untuk setiap OPD. */
    private static final List<String> DAFTAR_BIDANG = List.of(
            "Sekretariat",
            "Bidang Infrastruktur Teknologi Informasi",
            "Bidang Layanan Aplikasi Informatika",
            "Bidang Komunikasi dan Informasi Publik",
            "Bidang Statistik",
            "Bidang Persandian dan Keamanan Informasi",
            "Bidang Teknologi Informasi dan Komunikasi"
    );

    /** Nama-nama dummy untuk jabatan. */
    private static final List<String> NAMA_KETUA = List.of(
            "Drs. H. Suhartono, M.Si", "Drs. Bambang Wijaya, MM",
            "Dra. Siti Nurhayati, M.Pd", "Ir. H. Agus Supriyanto, MT",
            "Hj. Dewi Sartika, S.Sos, M.Si", "Dr. H. Mochammad Rizal, SE, MM",
            "Drs. H. Abdul Malik, M.M.", "H. Sutrisno, S.Pd, M.Kes",
            "Dra. Hj. Endang Purwanti, M.Si", "Ir. H. Sunaryo, MT"
    );
    private static final List<String> NAMA_SEKRETARIS = List.of(
            "Rini Ambarwati, S.Kom", "Hendra Gunawan, S.Sos",
            "Yuliana Dewi, S.E., M.M.", "Arief Rachman, S.T., M.T.",
            "Fitri Handayani, S.IP", "Eko Prasetyo, S.Kom., M.Kom",
            "Nina Marlina, S.E.", "Doni Kusuma, S.Sos., M.Si",
            "Ratna Puspita, S.T.", "Adi Saputra, S.Kom"
    );
    private static final List<String> NAMA_BENDAHARA = List.of(
            "Tuti Alawiyah, S.E.", "Agus Salim, S.E.",
            "Lilis Suryani, A.Md", "Bambang Santoso, S.E.",
            "Rina Sulistiani, S.Kom", "Fajar Nugroho, S.E.",
            "Diana Permata Sari, S.E.", "Haryanto, S.E., M.M.",
            "Ani Rahmawati, A.Md", "Slamet Riyadi, S.E."
    );
    private static final List<String> NAMA_ANGGOTA = List.of(
            "Ahmad Fauzi", "Budi Hartono", "Citra Dewi Lestari",
            "Deni Prasetyo", "Eka Fitriani", "Fajar Setiawan",
            "Gita Permata Sari", "Hendra Kusuma", "Indah Permatasari",
            "Joko Susilo", "Kartika Sari Dewi", "Luki Ardiansyah",
            "Mega Puspita Rini", "Nugroho Adi Wibowo", "Olivia Putri Maharani",
            "Prasetyo Budi Utomo", "Ratna Dewi Kusumawati", "Rizky Fadhilah",
            "Sari Dewi Anggraini", "Taufik Hidayat", "Umi Kalsum",
            "Vina Aulia Rahmah", "Wahyu Nugroho", "Yuni Astuti"
    );

    @Override
    public void run(String... args) {
        // Idempotent: cek apakah data wilayah sudah ada
        if (wilayahRepository.count() > 0) {
            return;
        }

        // 1. Buat root Provinsi Jawa Timur
        Wilayah provinsi = wilayahRepository.save(Wilayah.builder()
                .nama("Jawa Timur")
                .tipe(TipeWilayah.PROVINSI)
                .parent(null)
                .build());

        // 2. Buat 38 Kabupaten/Kota sebagai child Provinsi
        for (int i = 0; i < DAFTAR_KAB_KOTA.size(); i++) {
            KabKota kabKota = DAFTAR_KAB_KOTA.get(i);
            Wilayah wilayah = wilayahRepository.save(Wilayah.builder()
                    .nama(kabKota.nama())
                    .tipe(kabKota.tipe())
                    .parent(provinsi)
                    .build());

            // 3. Buat 1 OPD per Kabupaten/Kota
            String kodeOpd = "OPD-" + String.format("%02d", (i + 1));
            String namaOpd = "Dinas Komunikasi dan Informatika " + kabKota.nama();
            Opd opd = opdRepository.save(Opd.builder()
                    .wilayah(wilayah)
                    .namaOpd(namaOpd)
                    .kodeOpd(kodeOpd)
                    .build());

            // 4. Buat 7 Bidang per OPD
            for (String namaBidang : DAFTAR_BIDANG) {
                Bidang bidang = bidangRepository.save(Bidang.builder()
                        .opd(opd)
                        .namaBidang(namaBidang)
                        .build());

                // 5. Buat Ketua
                jabatanStrukturRepository.save(JabatanStruktur.builder()
                        .bidang(bidang)
                        .jabatan(Jabatan.KETUA)
                        .namaDummy(pickRandom(NAMA_KETUA))
                        .build());

                // 6. Buat Sekretaris
                jabatanStrukturRepository.save(JabatanStruktur.builder()
                        .bidang(bidang)
                        .jabatan(Jabatan.SEKRETARIS)
                        .namaDummy(pickRandom(NAMA_SEKRETARIS))
                        .build());

                // 7. Buat Bendahara
                jabatanStrukturRepository.save(JabatanStruktur.builder()
                        .bidang(bidang)
                        .jabatan(Jabatan.BENDAHARA)
                        .namaDummy(pickRandom(NAMA_BENDAHARA))
                        .build());

                // 8. Buat 3 Anggota
                for (int a = 0; a < 3; a++) {
                    jabatanStrukturRepository.save(JabatanStruktur.builder()
                            .bidang(bidang)
                            .jabatan(Jabatan.ANGGOTA)
                            .namaDummy(pickRandom(NAMA_ANGGOTA))
                            .build());
                }
            }
        }
    }

    /**
     * Pilih nama acak dari daftar.
     * Sederhana: pakai hashCode index untuk variasi deterministik per panggilan.
     */
    private String pickRandom(List<String> names) {
        int index = (int) (System.nanoTime() % names.size());
        if (index < 0) index = -index;
        return names.get(index);
    }

    /** Record sederhana untuk pasangan nama + tipe kab/kota. */
    private record KabKota(String nama, TipeWilayah tipe) {}
}

