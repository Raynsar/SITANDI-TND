package com.sipenjam.peminjamanalattu.model;

/**
 * Status siklus hidup sebuah pengajuan peminjaman.
 * MENUNGGU -> DISETUJUI -> DIPINJAM -> DIKEMBALIKAN
 *          -> DITOLAK
 *          -> DIBATALKAN (oleh peminjam, hanya saat masih MENUNGGU)
 */
public enum StatusPeminjaman {
    MENUNGGU,
    DISETUJUI,
    DITOLAK,
    DIPINJAM,
    DIKEMBALIKAN,
    DIBATALKAN
}
