package com.sitandi.controller.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.repository.BidangRepository;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.PenggunaRepository;
import com.sitandi.repository.WilayahRepository;

import lombok.RequiredArgsConstructor;

/**
 * REST API untuk statistik dashboard admin.
 *
 * Response JSON:
 * { "totalWilayah": N, "totalOpd": N, "totalBidang": N, "totalPersonal": N, "totalPengguna": N }
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final WilayahRepository wilayahRepository;
    private final OpdRepository opdRepository;
    private final BidangRepository bidangRepository;
    private final JabatanStrukturRepository jabatanStrukturRepository;
    private final PenggunaRepository penggunaRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalWilayah", wilayahRepository.count());
        stats.put("totalOpd", opdRepository.count());
        stats.put("totalBidang", bidangRepository.count());
        stats.put("totalPersonal", jabatanStrukturRepository.count());
        stats.put("totalPengguna", penggunaRepository.count());
        return ResponseEntity.ok(stats);
    }
}
