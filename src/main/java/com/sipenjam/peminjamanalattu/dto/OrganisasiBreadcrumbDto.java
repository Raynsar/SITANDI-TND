package com.sipenjam.peminjamanalattu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response untuk GET /api/v1/organisasi/breadcrumb/{id}.
 *
 * <p>Mengembalikan jalur lengkap dari root (Provinsi) sampai node yang diminta.
 * Klien tidak lagi menghitung breadcrumb sendiri dari tree — sebelumnya lookup
 * parent dilakukan secara linear dari index node FE, dan rentan salah ketika
 * ada beberapa node dengan nama mirip di pohon yang berbeda. Backend sekarang
 * yang menjamin traversal lewat parent_id dari database sehingga breadcrumb
 * selalu akurat untuk node manapun (Wilayah / OPD / Bidang).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisasiBreadcrumbDto {

    /** Path nama node dari root ke node target, mis. ["Jawa Timur", "Kota Surabaya", "Dinas Komunikasi dan Informatika Kota Surabaya", "Bidang ..."]. */
    private List<String> path;

    /** String breadcrumb siap-tampil, gabungan path dengan separator ' › '. */
    private String display;
}
