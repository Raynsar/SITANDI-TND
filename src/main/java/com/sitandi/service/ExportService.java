package com.sitandi.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.sitandi.dto.OrganisasiPersonalDto;
import com.sitandi.dto.PenggunaDto;

/**
 * Export data ke format Excel (.xlsx via Apache POI) dan PDF (via OpenPDF).
 */
@Service
public class ExportService {

    private static final String[] HEADER_PERSONAL = {"No", "Nama", "Jabatan", "NIP", "Username", "Email", "No HP"};
    private static final String[] HEADER_PENGGUNA = {"No", "Nama", "Username", "Email", "Role", "Status"};

    // ================= EXCEL =================

    public byte[] personalKeExcel(List<OrganisasiPersonalDto> data) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Personal");
            tulisHeader(wb, sheet, HEADER_PERSONAL);
            int no = 1;
            int r = 1;
            for (OrganisasiPersonalDto p : data) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(no++);
                row.createCell(1).setCellValue(nullSafe(p.getNama()));
                row.createCell(2).setCellValue(nullSafe(p.getJabatan()));
                row.createCell(3).setCellValue(nullSafe(p.getNip()));
                row.createCell(4).setCellValue(nullSafe(p.getUsername()));
                row.createCell(5).setCellValue(nullSafe(p.getEmail()));
                row.createCell(6).setCellValue(nullSafe(p.getNoHp()));
            }
            autoSize(sheet, HEADER_PERSONAL.length);
            return toBytes(wb);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal membuat file Excel: " + e.getMessage(), e);
        }
    }

    public byte[] penggunaKeExcel(List<PenggunaDto> data) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pengguna");
            tulisHeader(wb, sheet, HEADER_PENGGUNA);
            int no = 1;
            int r = 1;
            for (PenggunaDto p : data) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(no++);
                row.createCell(1).setCellValue(nullSafe(p.getNama()));
                row.createCell(2).setCellValue(nullSafe(p.getUsername()));
                row.createCell(3).setCellValue(nullSafe(p.getEmail()));
                row.createCell(4).setCellValue(nullSafe(p.getRole()));
                row.createCell(5).setCellValue(p.isAktif() ? "Aktif" : "Nonaktif");
            }
            autoSize(sheet, HEADER_PENGGUNA.length);
            return toBytes(wb);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal membuat file Excel: " + e.getMessage(), e);
        }
    }

    private void tulisHeader(Workbook wb, Sheet sheet, String[] headers) {
        Font font = wb.createFont();
        font.setBold(true);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook wb) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    // ================= PDF =================

    public byte[] personalKePdf(List<OrganisasiPersonalDto> data, String judul) {
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        PdfPTable table = tabel(7);
        tambahBarisHeader(table, HEADER_PERSONAL);
        int no = 1;
        for (OrganisasiPersonalDto p : data) {
            table.addCell(String.valueOf(no++));
            table.addCell(nullSafe(p.getNama()));
            table.addCell(nullSafe(p.getJabatan()));
            table.addCell(nullSafe(p.getNip()));
            table.addCell(nullSafe(p.getUsername()));
            table.addCell(nullSafe(p.getEmail()));
            table.addCell(nullSafe(p.getNoHp()));
        }
        return bangunPdf(judul, titleFont, table);
    }

    public byte[] penggunaKePdf(List<PenggunaDto> data, String judul) {
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        PdfPTable table = tabel(6);
        tambahBarisHeader(table, HEADER_PENGGUNA);
        int no = 1;
        for (PenggunaDto p : data) {
            table.addCell(String.valueOf(no++));
            table.addCell(nullSafe(p.getNama()));
            table.addCell(nullSafe(p.getUsername()));
            table.addCell(nullSafe(p.getEmail()));
            table.addCell(nullSafe(p.getRole()));
            table.addCell(p.isAktif() ? "Aktif" : "Nonaktif");
        }
        return bangunPdf(judul, titleFont, table);
    }

    private PdfPTable tabel(int kolom) {
        PdfPTable table = new PdfPTable(kolom);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        return table;
    }

    private void tambahBarisHeader(PdfPTable table, String[] headers) {
        com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(new java.awt.Color(173, 216, 230));
            table.addCell(cell);
        }
    }

    private byte[] bangunPdf(String judul, com.lowagie.text.Font titleFont, PdfPTable table) {
        try {
            Document doc = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(judul, titleFont));
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Gagal membuat file PDF: " + e.getMessage(), e);
        }
    }
}
