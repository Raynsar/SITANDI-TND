package com.sitandi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sitandi.dto.OrganisasiBreadcrumbDto;
import com.sitandi.dto.OrganisasiPersonalDto;
import com.sitandi.dto.OrganisasiTreeNodeDto;
import com.sitandi.model.Bidang;
import com.sitandi.model.JabatanStruktur;
import com.sitandi.model.Opd;
import com.sitandi.model.Wilayah;
import com.sitandi.repository.BidangRepository;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.WilayahRepository;
import com.sitandi.service.OrganisasiService;

import com.sitandi.dto.OrganisasiPersonalRequestDto;
import com.sitandi.exception.ResourceNotFoundException;
import com.sitandi.model.Jabatan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganisasiServiceImpl implements OrganisasiService {

    private final WilayahRepository wilayahRepository;
    private final OpdRepository opdRepository;
    private final BidangRepository bidangRepository;
    private final JabatanStrukturRepository jabatanStrukturRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrganisasiTreeNodeDto> getTree() {
        List<Wilayah> roots = wilayahRepository.findByParentIsNullOrderByNama();
        return roots.stream()
                .map(this::buildWilayahNode)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisasiTreeNodeDto getWilayahWithChildren(Long wilayahId) {
        Wilayah wilayah = wilayahRepository.findById(wilayahId).orElse(null);
        if (wilayah == null) return null;
        return buildWilayahNode(wilayah);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisasiPersonalDto> getPersonal(Long bidangId) {
        return jabatanStrukturRepository.findByBidangIdOrderByJabatan(bidangId).stream()
                .map(this::toPersonalDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisasiBreadcrumbDto getBreadcrumb(String tipe, Long id) {
        if (tipe == null || id == null) {
            return null;
        }
        String t = tipe.trim().toUpperCase();

        switch (t) {
            case "PROVINSI":
            case "KABUPATEN":
            case "KOTA": {
                Wilayah w = wilayahRepository.findById(id).orElse(null);
                if (w == null) return null;
                List<String> path = new ArrayList<>();
                Wilayah cursor = w;
                while (cursor != null) {
                    path.add(0, cursor.getNama());
                    cursor = cursor.getParent();
                }
                return buildBreadcrumbDto(path);
            }
            case "OPD": {
                Opd opd = opdRepository.findById(id).orElse(null);
                if (opd == null) return null;
                List<String> path = new ArrayList<>();
                path.add(0, opd.getNamaOpd());
                Wilayah cursor = opd.getWilayah();
                while (cursor != null) {
                    path.add(0, cursor.getNama());
                    cursor = cursor.getParent();
                }
                return buildBreadcrumbDto(path);
            }
            case "BIDANG": {
                Bidang bidang = bidangRepository.findById(id).orElse(null);
                if (bidang == null) return null;
                List<String> path = new ArrayList<>();
                path.add(0, bidang.getNamaBidang());
                Opd opdParent = bidang.getOpd();
                path.add(0, opdParent.getNamaOpd());
                Wilayah cursor = opdParent.getWilayah();
                while (cursor != null) {
                    path.add(0, cursor.getNama());
                    cursor = cursor.getParent();
                }
                return buildBreadcrumbDto(path);
            }
            default:
                return null;
        }
    }

    private OrganisasiBreadcrumbDto buildBreadcrumbDto(List<String> path) {
        String display = String.join(" › ", path);
        return OrganisasiBreadcrumbDto.builder()
                .path(path)
                .display(display)
                .build();
    }

    private OrganisasiPersonalDto toPersonalDto(JabatanStruktur js) {
        return OrganisasiPersonalDto.builder()
                .id(js.getId())
                .nama(js.getNamaDummy())
                .jabatan(js.getJabatan().name())
                .username(js.getUsername())
                .build();
    }

    private OrganisasiTreeNodeDto buildWilayahNode(Wilayah wilayah) {
        OrganisasiTreeNodeDto node = new OrganisasiTreeNodeDto();
        node.setId(wilayah.getId());
        node.setNama(wilayah.getNama());
        node.setTipe(wilayah.getTipe().name());

        List<Wilayah> childWilayah = wilayahRepository.findByParentIdOrderByNama(wilayah.getId());
        List<OrganisasiTreeNodeDto> children = childWilayah.stream()
                .map(this::buildWilayahNode)
                .toList();

        List<Opd> opds = opdRepository.findByWilayahIdOrderByNamaOpd(wilayah.getId());
        List<OrganisasiTreeNodeDto> opdNodes = opds.stream()
                .map(this::buildOpdNode)
                .toList();

        children = mergeLists(children, opdNodes);
        node.setChildren(children);
        return node;
    }

    private OrganisasiTreeNodeDto buildOpdNode(Opd opd) {
        OrganisasiTreeNodeDto node = new OrganisasiTreeNodeDto();
        node.setId(opd.getId());
        node.setNama(opd.getNamaOpd());
        node.setTipe("OPD");

        List<Bidang> bidangList = bidangRepository.findByOpdIdOrderByNamaBidang(opd.getId());
        List<OrganisasiTreeNodeDto> bidangNodes = bidangList.stream()
                .map(this::buildBidangNode)
                .toList();
        node.setChildren(bidangNodes);
        return node;
    }

    private OrganisasiTreeNodeDto buildBidangNode(Bidang bidang) {
        OrganisasiTreeNodeDto node = new OrganisasiTreeNodeDto();
        node.setId(bidang.getId());
        node.setNama(bidang.getNamaBidang());
        node.setTipe("BIDANG");
        node.setChildren(List.of());
        return node;
    }

    /**
     * Gabung dua list child node jadi satu list.
     * Dipakai untuk menggabung child wilayah + child OPD di satu level.
     */
    private List<OrganisasiTreeNodeDto> mergeLists(List<OrganisasiTreeNodeDto> a,
                                                    List<OrganisasiTreeNodeDto> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }
    @Override
    @Transactional
    public OrganisasiPersonalDto createPersonal(Long bidangId, OrganisasiPersonalRequestDto dto) {
        Bidang bidang = bidangRepository.findById(bidangId)
                .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan dengan id: " + bidangId));
        
        if (jabatanStrukturRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username sudah digunakan: " + dto.getUsername());
        }

        Jabatan jabatanEnum;
        try {
            jabatanEnum = Jabatan.valueOf(dto.getJabatan().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Jabatan tidak valid: " + dto.getJabatan());
        }

        JabatanStruktur js = JabatanStruktur.builder()
                .bidang(bidang)
                .namaDummy(dto.getNama())
                .jabatan(jabatanEnum)
                .username(dto.getUsername())
                .build();
        
        js = jabatanStrukturRepository.save(js);
        return toPersonalDto(js);
    }

    @Override
    @Transactional
    public OrganisasiPersonalDto updatePersonal(Long id, OrganisasiPersonalRequestDto dto) {
        JabatanStruktur js = jabatanStrukturRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personel tidak ditemukan dengan id: " + id));
        
        if (jabatanStrukturRepository.existsByUsernameAndIdNot(dto.getUsername(), id)) {
            throw new IllegalArgumentException("Username sudah digunakan: " + dto.getUsername());
        }

        Jabatan jabatanEnum;
        try {
            jabatanEnum = Jabatan.valueOf(dto.getJabatan().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Jabatan tidak valid: " + dto.getJabatan());
        }

        js.setNamaDummy(dto.getNama());
        js.setJabatan(jabatanEnum);
        js.setUsername(dto.getUsername());
        
        js = jabatanStrukturRepository.save(js);
        return toPersonalDto(js);
    }

    @Override
    @Transactional
    public void deletePersonal(Long id) {
        if (!jabatanStrukturRepository.existsById(id)) {
            throw new ResourceNotFoundException("Personel tidak ditemukan dengan id: " + id);
        }
        jabatanStrukturRepository.deleteById(id);
    }
}

