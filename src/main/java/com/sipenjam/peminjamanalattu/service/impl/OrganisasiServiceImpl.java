package com.sipenjam.peminjamanalattu.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sipenjam.peminjamanalattu.dto.OrganisasiTreeNodeDto;
import com.sipenjam.peminjamanalattu.model.Bidang;
import com.sipenjam.peminjamanalattu.model.JabatanStruktur;
import com.sipenjam.peminjamanalattu.model.Opd;
import com.sipenjam.peminjamanalattu.model.Wilayah;
import com.sipenjam.peminjamanalattu.repository.BidangRepository;
import com.sipenjam.peminjamanalattu.repository.JabatanStrukturRepository;
import com.sipenjam.peminjamanalattu.repository.OpdRepository;
import com.sipenjam.peminjamanalattu.repository.WilayahRepository;
import com.sipenjam.peminjamanalattu.service.OrganisasiService;

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
        // Ambil semua root nodes (Provinsi)
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

    private OrganisasiTreeNodeDto buildWilayahNode(Wilayah wilayah) {
        OrganisasiTreeNodeDto node = new OrganisasiTreeNodeDto();
        node.setId(wilayah.getId());
        node.setNama(wilayah.getNama());
        node.setTipe(wilayah.getTipe().name());

        // Ambil child wilayah (kab/kota)
        List<Wilayah> childWilayah = wilayahRepository.findByParentIdOrderByNama(wilayah.getId());
        List<OrganisasiTreeNodeDto> children = childWilayah.stream()
                .map(this::buildWilayahNode)
                .toList();

        // Ambil OPD di wilayah ini
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

        List<JabatanStruktur> jabatanList = jabatanStrukturRepository.findByBidangIdOrderByJabatan(bidang.getId());
        List<OrganisasiTreeNodeDto> jabatanNodes = jabatanList.stream()
                .map(this::buildJabatanNode)
                .toList();
        node.setChildren(jabatanNodes);
        return node;
    }

    private OrganisasiTreeNodeDto buildJabatanNode(JabatanStruktur js) {
        OrganisasiTreeNodeDto node = new OrganisasiTreeNodeDto();
        node.setId(js.getId());
        node.setNama(js.getJabatan() + ": " + js.getNamaDummy());
        node.setTipe("JABATAN");
        node.setChildren(List.of()); // leaf node, no children
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
}

