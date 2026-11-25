package com.sitandi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sitandi.dto.GlobalSearchResultDto;
import com.sitandi.model.Bidang;
import com.sitandi.model.Jabatan;
import com.sitandi.model.JabatanStruktur;
import com.sitandi.model.Opd;
import com.sitandi.model.Pengguna;
import com.sitandi.model.Role;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.PenggunaRepository;

/**
 * Unit test Global Search. Test Double: Stub (hasil repository disiapkan) dan
 * Mock (verifikasi repository tidak dipanggil untuk keyword pendek).
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private JabatanStrukturRepository jabatanStrukturRepository;
    @Mock
    private OpdRepository opdRepository;
    @Mock
    private PenggunaRepository penggunaRepository;

    @InjectMocks
    private SearchServiceImpl service;

    private JabatanStruktur personel(long id) {
        Bidang bidang = Bidang.builder().id(100L).namaBidang("Bidang Aplikasi").build();
        return JabatanStruktur.builder()
                .id(id).bidang(bidang).jabatan(Jabatan.ANGGOTA).namaDummy("Ahmad Fauzi")
                .username("afauzi").nip("199001012020011001").email("a@b.go.id").noHp("0812")
                .build();
    }

    private Opd opd(long id) {
        return Opd.builder().id(id).namaOpd("Dinas Kominfo " + id).kodeOpd("K" + id).build();
    }

    private Pengguna user(long id) {
        return Pengguna.builder().id(id).nama("User " + id).username("user" + id)
                .role(Role.USER).aktif(true).password("x").build();
    }

    @Test
    @DisplayName("search: keyword null -> tiga kategori kosong, repository tidak disentuh")
    void search_keywordNull() {
        GlobalSearchResultDto hasil = service.search(null);

        assertThat(hasil.getPersonal()).isEmpty();
        assertThat(hasil.getOpd()).isEmpty();
        assertThat(hasil.getUser()).isEmpty();
        verifyNoInteractions(jabatanStrukturRepository, opdRepository, penggunaRepository);
    }

    @Test
    @DisplayName("search: keyword 1 karakter (setelah trim) -> kosong")
    void search_keywordSatuKarakter() {
        GlobalSearchResultDto hasil = service.search("  a ");

        assertThat(hasil.getPersonal()).isEmpty();
        verifyNoInteractions(jabatanStrukturRepository, opdRepository, penggunaRepository);
    }

    @Test
    @DisplayName("search: keyword valid di-trim dan hasil dipetakan per kategori")
    void search_valid_dipetakan() {
        when(jabatanStrukturRepository
                .findByNamaDummyContainingIgnoreCaseOrNipContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        "ahmad", "ahmad", "ahmad"))
                .thenReturn(List.of(personel(7L)));
        when(opdRepository.findByNamaOpdContainingIgnoreCase("ahmad")).thenReturn(List.of(opd(10L)));
        when(penggunaRepository.findByNamaContainingIgnoreCaseOrUsernameContainingIgnoreCase("ahmad", "ahmad"))
                .thenReturn(List.of(user(3L)));

        GlobalSearchResultDto hasil = service.search("  ahmad  ");

        assertThat(hasil.getPersonal()).hasSize(1);
        assertThat(hasil.getPersonal().get(0).getJabatan()).isEqualTo("ANGGOTA");
        assertThat(hasil.getPersonal().get(0).getBidangId()).isEqualTo(100L);
        assertThat(hasil.getOpd().get(0).getNama()).isEqualTo("Dinas Kominfo 10");
        assertThat(hasil.getUser().get(0).getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("search: tiap kategori dibatasi maksimal 10 hasil")
    void search_dibatasiSepuluhPerKategori() {
        List<Opd> banyak = new ArrayList<>();
        for (long i = 1; i <= 15; i++) {
            banyak.add(opd(i));
        }
        when(opdRepository.findByNamaOpdContainingIgnoreCase("dinas")).thenReturn(banyak);

        GlobalSearchResultDto hasil = service.search("dinas");

        assertThat(hasil.getOpd()).hasSize(10);
        assertThat(hasil.getPersonal()).isEmpty();
        assertThat(hasil.getUser()).isEmpty();
    }
}
