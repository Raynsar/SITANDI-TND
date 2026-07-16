package com.sipenjam.peminjamanalattu.dto;

import java.time.LocalDate;

import com.sipenjam.peminjamanalattu.model.Peminjaman;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representasi peminjaman untuk riwayat (user) & persetujuan (admin).
 * Nama field mengikuti template: namaAlat, namaPemohon, tanggalPinjam, tanggalKembali, status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeminjamanResponseDto {

    private Long id;
    private String namaAlat;
    private String namaPemohon;
    private LocalDate tanggalPinjam;
    private LocalDate tanggalKembali;
    private LocalDate tanggalDikembalikan;
    private String keperluan;
    private String status;
    private String kondisiSaatKembali;
    private String catatanPengembalian;

    public static PeminjamanResponseDto fromEntity(Peminjaman p) {
        return PeminjamanResponseDto.builder()
                .id(p.getId())
                .namaAlat(p.getAlat() != null ? p.getAlat().getNama() : null)
                .namaPemohon(p.getPengguna() != null ? p.getPengguna().getNama() : null)
                .tanggalPinjam(p.getTanggalPinjam())
                .tanggalKembali(p.getTanggalKembaliRencana())
                .tanggalDikembalikan(p.getTanggalDikembalikan())
                .keperluan(p.getKeperluan())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .kondisiSaatKembali(p.getKondisiSaatKembali() != null ? p.getKondisiSaatKembali().name() : null)
                .catatanPengembalian(p.getCatatanPengembalian())
                .build();
    }
}
