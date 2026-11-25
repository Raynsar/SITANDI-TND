package com.sitandi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.sitandi.dto.OrganisasiBreadcrumbDto;
import com.sitandi.dto.OrganisasiPersonalDto;
import com.sitandi.dto.OrganisasiPersonalRequestDto;
import com.sitandi.dto.OrganisasiSearchResultDto;
import com.sitandi.dto.OrganisasiTreeNodeDto;
import com.sitandi.exception.ResourceNotFoundException;
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

/**
 * Unit test fitur Struktur Organisasi (tree, breadcrumb, personal CRUD, pencarian).
 *
 * Test Double: Stub (repository mengembalikan data hierarki siap pakai) dan
 * Mock (verifikasi save/deleteById tidak dipanggil pada jalur error).
 * Strictness LENIENT dipakai karena membangun tree memanggil repository yang sama
 * secara rekursif dengan argumen berbeda.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganisasiServiceImplTest {

    private static final String SEP = " › ";

    @Mock
    private WilayahRepository wilayahRepository;
    @Mock
    private OpdRepository opdRepository;
    @Mock
    private BidangRepository bidangRepository;
    @Mock
    private JabatanStrukturRepository jabatanStrukturRepository;

    @InjectMocks
    private OrganisasiServiceImpl service;

    // ---------- data bantu ----------

    private Wilayah provinsi() {
        return Wilayah.builder().id(1L).nama("Jawa Timur").tipe(TipeWilayah.PROVINSI).build();
    }

    private Wilayah kota(Wilayah parent) {
        return Wilayah.builder().id(2L).nama("Kota Surabaya").tipe(TipeWilayah.KOTA).parent(parent).build();
    }

    private Opd opd(Wilayah wilayah) {
        return Opd.builder().id(10L).namaOpd("Dinas Kominfo").kodeOpd("KMF").wilayah(wilayah).build();
    }

    private Bidang bidang(Opd opd) {
        return Bidang.builder().id(100L).namaBidang("Bidang Aplikasi").opd(opd).build();
    }

    private JabatanStruktur personel(Bidang bidang, Long id, String username) {
        return JabatanStruktur.builder()
                .id(id).bidang(bidang).jabatan(Jabatan.KETUA).namaDummy("Ahmad Fauzi")
                .username(username).nip("199001012020011001").email("afauzi@kominfo.go.id").noHp("0812-3456-7890")
                .build();
    }

    private OrganisasiPersonalRequestDto request(String jabatan, String username) {
        return OrganisasiPersonalRequestDto.builder()
                .nama("Budi Santoso").jabatan(jabatan).username(username)
                .nip("199505052021011002").email("bsantoso@kominfo.go.id").noHp("0813-0000-1111")
                .build();
    }

    // ================= getTree & getWilayahWithChildren =================

    @Test
    @DisplayName("getTree: provinsi -> kota -> OPD -> bidang tersusun bertingkat")
    void getTree_hierarkiLengkap() {
        Wilayah prov = provinsi();
        Wilayah kota = kota(prov);
        Opd opd = opd(kota);
        Bidang bidang = bidang(opd);
        when(wilayahRepository.findByParentIsNullOrderByNama()).thenReturn(List.of(prov));
        when(wilayahRepository.findByParentIdOrderByNama(1L)).thenReturn(List.of(kota));
        when(opdRepository.findByWilayahIdOrderByNamaOpd(2L)).thenReturn(List.of(opd));
        when(bidangRepository.findByOpdIdOrderByNamaBidang(10L)).thenReturn(List.of(bidang));

        List<OrganisasiTreeNodeDto> tree = service.getTree();

        assertThat(tree).hasSize(1);
        OrganisasiTreeNodeDto root = tree.get(0);
        assertThat(root.getNama()).isEqualTo("Jawa Timur");
        assertThat(root.getTipe()).isEqualTo("PROVINSI");
        OrganisasiTreeNodeDto kotaNode = root.getChildren().get(0);
        assertThat(kotaNode.getTipe()).isEqualTo("KOTA");
        OrganisasiTreeNodeDto opdNode = kotaNode.getChildren().get(0);
        assertThat(opdNode.getTipe()).isEqualTo("OPD");
        assertThat(opdNode.getChildren().get(0).getTipe()).isEqualTo("BIDANG");
        assertThat(opdNode.getChildren().get(0).getChildren()).isEmpty();
    }

    @Test
    @DisplayName("getTree: tidak ada wilayah -> list kosong")
    void getTree_kosong() {
        when(wilayahRepository.findByParentIsNullOrderByNama()).thenReturn(List.of());

        assertThat(service.getTree()).isEmpty();
    }

    @Test
    @DisplayName("getWilayahWithChildren: id ada -> node dikembalikan")
    void getWilayahWithChildren_ada() {
        when(wilayahRepository.findById(1L)).thenReturn(Optional.of(provinsi()));

        OrganisasiTreeNodeDto node = service.getWilayahWithChildren(1L);

        assertThat(node).isNotNull();
        assertThat(node.getNama()).isEqualTo("Jawa Timur");
    }

    @Test
    @DisplayName("getWilayahWithChildren: id tidak ada -> null")
    void getWilayahWithChildren_tidakAda_null() {
        when(wilayahRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getWilayahWithChildren(99L)).isNull();
    }

    // ================= getPersonal =================

    @Test
    @DisplayName("getPersonal: entity dipetakan ke DTO")
    void getPersonal_dipetakan() {
        when(jabatanStrukturRepository.findByBidangIdOrderByJabatan(100L))
                .thenReturn(List.of(personel(bidang(opd(kota(provinsi()))), 7L, "afauzi")));

        List<OrganisasiPersonalDto> hasil = service.getPersonal(100L);

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getNama()).isEqualTo("Ahmad Fauzi");
        assertThat(hasil.get(0).getJabatan()).isEqualTo("KETUA");
        assertThat(hasil.get(0).getUsername()).isEqualTo("afauzi");
    }

    // ================= getBreadcrumb =================

    @Test
    @DisplayName("getBreadcrumb: tipe null atau id null -> null")
    void getBreadcrumb_parameterNull() {
        assertThat(service.getBreadcrumb(null, 1L)).isNull();
        assertThat(service.getBreadcrumb("KOTA", null)).isNull();
        verifyNoInteractions(wilayahRepository, opdRepository, bidangRepository);
    }

    @Test
    @DisplayName("getBreadcrumb KOTA: jalur dari provinsi sampai kota")
    void getBreadcrumb_kota() {
        Wilayah kota = kota(provinsi());
        when(wilayahRepository.findById(2L)).thenReturn(Optional.of(kota));

        OrganisasiBreadcrumbDto hasil = service.getBreadcrumb("KOTA", 2L);

        assertThat(hasil.getPath()).containsExactly("Jawa Timur", "Kota Surabaya");
        assertThat(hasil.getDisplay()).isEqualTo("Jawa Timur" + SEP + "Kota Surabaya");
    }

    @Test
    @DisplayName("getBreadcrumb PROVINSI: hanya satu elemen")
    void getBreadcrumb_provinsi() {
        when(wilayahRepository.findById(1L)).thenReturn(Optional.of(provinsi()));

        assertThat(service.getBreadcrumb("provinsi", 1L).getPath()).containsExactly("Jawa Timur");
    }

    @Test
    @DisplayName("getBreadcrumb wilayah tidak ditemukan -> null")
    void getBreadcrumb_wilayahTidakAda() {
        when(wilayahRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getBreadcrumb("KABUPATEN", 99L)).isNull();
    }

    @Test
    @DisplayName("getBreadcrumb OPD: wilayah induk + nama OPD")
    void getBreadcrumb_opd() {
        when(opdRepository.findById(10L)).thenReturn(Optional.of(opd(kota(provinsi()))));

        OrganisasiBreadcrumbDto hasil = service.getBreadcrumb("  opd ", 10L); // di-trim dan di-uppercase

        assertThat(hasil.getPath()).containsExactly("Jawa Timur", "Kota Surabaya", "Dinas Kominfo");
    }

    @Test
    @DisplayName("getBreadcrumb OPD tidak ditemukan -> null")
    void getBreadcrumb_opdTidakAda() {
        when(opdRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getBreadcrumb("OPD", 99L)).isNull();
    }

    @Test
    @DisplayName("getBreadcrumb BIDANG: wilayah + OPD + bidang")
    void getBreadcrumb_bidang() {
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang(opd(kota(provinsi())))));

        OrganisasiBreadcrumbDto hasil = service.getBreadcrumb("BIDANG", 100L);

        assertThat(hasil.getPath())
                .containsExactly("Jawa Timur", "Kota Surabaya", "Dinas Kominfo", "Bidang Aplikasi");
    }

    @Test
    @DisplayName("getBreadcrumb BIDANG tidak ditemukan -> null")
    void getBreadcrumb_bidangTidakAda() {
        when(bidangRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getBreadcrumb("BIDANG", 99L)).isNull();
    }

    @Test
    @DisplayName("getBreadcrumb tipe tidak dikenal -> null")
    void getBreadcrumb_tipeTidakDikenal() {
        assertThat(service.getBreadcrumb("RT", 1L)).isNull();
    }

    // ================= createPersonal =================

    @Test
    @DisplayName("createPersonal valid: tersimpan, jabatan huruf kecil diterima")
    void createPersonal_valid() {
        Bidang bidang = bidang(opd(kota(provinsi())));
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang));
        when(jabatanStrukturRepository.existsByUsername("bsantoso")).thenReturn(false);
        when(jabatanStrukturRepository.save(any(JabatanStruktur.class))).thenAnswer(inv -> {
            JabatanStruktur js = inv.getArgument(0);
            js.setId(55L);
            return js;
        });

        OrganisasiPersonalDto hasil = service.createPersonal(100L, request("sekretaris", "bsantoso"));

        assertThat(hasil.getId()).isEqualTo(55L);
        assertThat(hasil.getJabatan()).isEqualTo("SEKRETARIS");
        ArgumentCaptor<JabatanStruktur> captor = ArgumentCaptor.forClass(JabatanStruktur.class);
        verify(jabatanStrukturRepository).save(captor.capture());
        assertThat(captor.getValue().getBidang()).isSameAs(bidang);
        assertThat(captor.getValue().getNamaDummy()).isEqualTo("Budi Santoso");
    }

    @Test
    @DisplayName("createPersonal: bidang tidak ada -> ResourceNotFoundException")
    void createPersonal_bidangTidakAda() {
        when(bidangRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPersonal(99L, request("KETUA", "bsantoso")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bidang tidak ditemukan");
    }

    @Test
    @DisplayName("createPersonal: username sudah dipakai -> IllegalArgumentException")
    void createPersonal_usernameDuplikat() {
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang(opd(kota(provinsi())))));
        when(jabatanStrukturRepository.existsByUsername("afauzi")).thenReturn(true);

        assertThatThrownBy(() -> service.createPersonal(100L, request("KETUA", "afauzi")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username sudah digunakan");

        verify(jabatanStrukturRepository, never()).save(any(JabatanStruktur.class));
    }

    @Test
    @DisplayName("createPersonal: jabatan tidak valid -> IllegalArgumentException")
    void createPersonal_jabatanTidakValid() {
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang(opd(kota(provinsi())))));

        assertThatThrownBy(() -> service.createPersonal(100L, request("DIREKTUR", "bsantoso")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Jabatan tidak valid: DIREKTUR");

        verify(jabatanStrukturRepository, never()).save(any(JabatanStruktur.class));
    }

    // ================= updatePersonal =================

    @Test
    @DisplayName("updatePersonal valid: field berubah dan disimpan")
    void updatePersonal_valid() {
        JabatanStruktur js = personel(bidang(opd(kota(provinsi()))), 7L, "afauzi");
        when(jabatanStrukturRepository.findById(7L)).thenReturn(Optional.of(js));
        when(jabatanStrukturRepository.existsByUsernameAndIdNot("bsantoso", 7L)).thenReturn(false);
        when(jabatanStrukturRepository.save(js)).thenReturn(js);

        OrganisasiPersonalDto hasil = service.updatePersonal(7L, request("BENDAHARA", "bsantoso"));

        assertThat(hasil.getNama()).isEqualTo("Budi Santoso");
        assertThat(hasil.getJabatan()).isEqualTo("BENDAHARA");
        assertThat(js.getUsername()).isEqualTo("bsantoso");
    }

    @Test
    @DisplayName("updatePersonal: id tidak ada -> ResourceNotFoundException")
    void updatePersonal_idTidakAda() {
        when(jabatanStrukturRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePersonal(99L, request("KETUA", "bsantoso")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updatePersonal: username dipakai personel lain -> IllegalArgumentException")
    void updatePersonal_usernameDuplikat() {
        when(jabatanStrukturRepository.findById(7L))
                .thenReturn(Optional.of(personel(bidang(opd(kota(provinsi()))), 7L, "afauzi")));
        when(jabatanStrukturRepository.existsByUsernameAndIdNot("bsantoso", 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.updatePersonal(7L, request("KETUA", "bsantoso")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username sudah digunakan");
    }

    @Test
    @DisplayName("updatePersonal: jabatan tidak valid -> IllegalArgumentException")
    void updatePersonal_jabatanTidakValid() {
        when(jabatanStrukturRepository.findById(7L))
                .thenReturn(Optional.of(personel(bidang(opd(kota(provinsi()))), 7L, "afauzi")));

        assertThatThrownBy(() -> service.updatePersonal(7L, request("MANAJER", "bsantoso")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Jabatan tidak valid: MANAJER");
    }

    // ================= deletePersonal =================

    @Test
    @DisplayName("deletePersonal: id ada -> deleteById dipanggil")
    void deletePersonal_ada() {
        when(jabatanStrukturRepository.existsById(7L)).thenReturn(true);

        service.deletePersonal(7L);

        verify(jabatanStrukturRepository).deleteById(7L);
    }

    @Test
    @DisplayName("deletePersonal: id tidak ada -> ResourceNotFoundException, tidak ada penghapusan")
    void deletePersonal_tidakAda() {
        when(jabatanStrukturRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePersonal(99L)).isInstanceOf(ResourceNotFoundException.class);

        verify(jabatanStrukturRepository, never()).deleteById(any(Long.class));
    }

    // ================= search =================

    @Test
    @DisplayName("search: keyword null atau kurang dari 2 karakter -> list kosong tanpa query")
    void search_keywordTerlaluPendek() {
        assertThat(service.search(null)).isEmpty();
        assertThat(service.search("a")).isEmpty();
        assertThat(service.search("  b ")).isEmpty();
        verifyNoInteractions(wilayahRepository, opdRepository, bidangRepository, jabatanStrukturRepository);
    }

    @Test
    @DisplayName("search: hasil wilayah, OPD, bidang dan personal digabung dengan path breadcrumb")
    void search_semuaKategori() {
        Wilayah kota = kota(provinsi());
        Opd opd = opd(kota);
        Bidang bidang = bidang(opd);
        when(wilayahRepository.findByNamaContainingIgnoreCase("kom")).thenReturn(List.of(kota));
        when(wilayahRepository.findById(2L)).thenReturn(Optional.of(kota));
        when(opdRepository.findByNamaOpdContainingIgnoreCase("kom")).thenReturn(List.of(opd));
        when(opdRepository.findById(10L)).thenReturn(Optional.of(opd));
        when(bidangRepository.findByNamaBidangContainingIgnoreCase("kom")).thenReturn(List.of(bidang));
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang));
        when(jabatanStrukturRepository.findByNamaDummyContainingIgnoreCaseOrNipContainingIgnoreCaseOrJabatanIn(
                eq("kom"), eq("kom"), isNull()))
                .thenReturn(List.of(personel(bidang, 7L, "afauzi")));

        List<OrganisasiSearchResultDto> hasil = service.search("  kom  ");

        assertThat(hasil).extracting(OrganisasiSearchResultDto::getTipe)
                .containsExactly("KOTA", "OPD", "BIDANG", "PERSONAL");
        assertThat(hasil.get(0).getPath()).isEqualTo("Jawa Timur" + SEP + "Kota Surabaya");
        assertThat(hasil.get(3).getBidangId()).isEqualTo(100L);
        assertThat(hasil.get(3).getJabatan()).isEqualTo("KETUA");
    }

    @Test
    @DisplayName("search: keyword cocok nama jabatan -> filter jabatan dikirim ke repository")
    void search_cocokNamaJabatan() {
        Bidang bidang = bidang(opd(kota(provinsi())));
        when(bidangRepository.findById(100L)).thenReturn(Optional.of(bidang));
        when(jabatanStrukturRepository.findByNamaDummyContainingIgnoreCaseOrNipContainingIgnoreCaseOrJabatanIn(
                eq("ketua"), eq("ketua"), eq(List.of(Jabatan.KETUA))))
                .thenReturn(List.of(personel(bidang, 7L, "afauzi")));

        List<OrganisasiSearchResultDto> hasil = service.search("ketua");

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getTipe()).isEqualTo("PERSONAL");
    }

    @Test
    @DisplayName("search: breadcrumb tidak ditemukan -> path memakai nama node (fallback)")
    void search_breadcrumbNull_fallbackNama() {
        when(wilayahRepository.findByNamaContainingIgnoreCase("surabaya"))
                .thenReturn(List.of(kota(provinsi())));
        when(wilayahRepository.findById(2L)).thenReturn(Optional.empty());

        List<OrganisasiSearchResultDto> hasil = service.search("surabaya");

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getPath()).isEqualTo("Kota Surabaya");
    }

    @Test
    @DisplayName("search: hasil dibatasi maksimal 50")
    void search_dibatasi50() {
        List<Wilayah> banyak = new ArrayList<>();
        for (long i = 1; i <= 60; i++) {
            banyak.add(Wilayah.builder().id(i).nama("Kota " + i).tipe(TipeWilayah.KOTA).build());
        }
        when(wilayahRepository.findByNamaContainingIgnoreCase("kota")).thenReturn(banyak);

        assertThat(service.search("kota")).hasSize(50);
    }
}
