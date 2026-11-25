package com.sitandi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sitandi.dto.OrganisasiPersonalDto;
import com.sitandi.dto.PenggunaDto;

/**
 * Unit test Export Excel/PDF. Tanpa Test Double: ExportService tidak punya dependency,
 * output byte[] dibaca ulang (Apache POI) untuk memastikan isinya benar.
 */
class ExportServiceTest {

    private final ExportService service = new ExportService();

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private Sheet bacaSheet(byte[] data) throws Exception {
        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
        return wb.getSheetAt(0);
    }

    private OrganisasiPersonalDto personal(String nama, String email) {
        return OrganisasiPersonalDto.builder().id(1L).nama(nama).jabatan("KETUA")
                .username("afauzi").nip("199001012020011001").email(email).noHp("0812-3456-7890").build();
    }

    private PenggunaDto pengguna(boolean aktif) {
        return PenggunaDto.builder().id(1L).nama("Adifa").username("adifa").email("adifa@qa.local")
                .role("USER").aktif(aktif).build();
    }

    // ================= Excel personal =================

    @Test
    @DisplayName("personalKeExcel: header dan baris data benar")
    void personalKeExcel_headerDanData() throws Exception {
        byte[] hasil = service.personalKeExcel(List.of(personal("Ahmad Fauzi", "a@b.go.id")));

        Sheet sheet = bacaSheet(hasil);
        assertThat(sheet.getSheetName()).isEqualTo("Personal");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Nama");
        assertThat(sheet.getRow(0).getCell(6).getStringCellValue()).isEqualTo("No HP");
        assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(1.0);
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Ahmad Fauzi");
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("KETUA");
        assertThat(sheet.getLastRowNum()).isEqualTo(1);
    }

    @Test
    @DisplayName("personalKeExcel: field null ditulis sebagai string kosong")
    void personalKeExcel_nullJadiKosong() throws Exception {
        byte[] hasil = service.personalKeExcel(List.of(personal("Ahmad Fauzi", null)));

        assertThat(bacaSheet(hasil).getRow(1).getCell(5).getStringCellValue()).isEmpty();
    }

    @Test
    @DisplayName("personalKeExcel: data kosong -> hanya baris header")
    void personalKeExcel_dataKosong() throws Exception {
        byte[] hasil = service.personalKeExcel(List.of());

        assertThat(bacaSheet(hasil).getLastRowNum()).isZero();
    }

    @Test
    @DisplayName("personalKeExcel: nomor urut bertambah per baris")
    void personalKeExcel_nomorUrut() throws Exception {
        byte[] hasil = service.personalKeExcel(List.of(personal("A", "a@x"), personal("B", "b@x")));

        Sheet sheet = bacaSheet(hasil);
        assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(1.0);
        assertThat(sheet.getRow(2).getCell(0).getNumericCellValue()).isEqualTo(2.0);
    }

    // ================= Excel pengguna =================

    @Test
    @DisplayName("penggunaKeExcel: status aktif -> 'Aktif', nonaktif -> 'Nonaktif'")
    void penggunaKeExcel_status() throws Exception {
        byte[] hasil = service.penggunaKeExcel(List.of(pengguna(true), pengguna(false)));

        Sheet sheet = bacaSheet(hasil);
        assertThat(sheet.getSheetName()).isEqualTo("Pengguna");
        assertThat(sheet.getRow(1).getCell(5).getStringCellValue()).isEqualTo("Aktif");
        assertThat(sheet.getRow(2).getCell(5).getStringCellValue()).isEqualTo("Nonaktif");
        assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo("USER");
    }

    // ================= PDF =================

    private void assertPdfValid(byte[] pdf) {
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("personalKePdf: menghasilkan PDF valid")
    void personalKePdf_valid() {
        assertPdfValid(service.personalKePdf(List.of(personal("Ahmad Fauzi", "a@b.go.id")), "Daftar Personal"));
    }

    @Test
    @DisplayName("personalKePdf: data kosong dan field null tetap menghasilkan PDF valid")
    void personalKePdf_kosongDanNull() {
        assertPdfValid(service.personalKePdf(List.of(), "Kosong"));
        assertPdfValid(service.personalKePdf(List.of(personal(null, null)), "Ada null"));
    }

    @Test
    @DisplayName("penggunaKePdf: menghasilkan PDF valid untuk akun aktif dan nonaktif")
    void penggunaKePdf_valid() {
        assertPdfValid(service.penggunaKePdf(List.of(pengguna(true), pengguna(false)), "Daftar Pengguna"));
    }
}
