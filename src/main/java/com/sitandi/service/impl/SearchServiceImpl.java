package com.sitandi.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sitandi.dto.GlobalSearchResultDto;
import com.sitandi.model.JabatanStruktur;
import com.sitandi.model.Opd;
import com.sitandi.model.Pengguna;
import com.sitandi.repository.JabatanStrukturRepository;
import com.sitandi.repository.OpdRepository;
import com.sitandi.repository.PenggunaRepository;
import com.sitandi.service.SearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int MAX_PER_KATEGORI = 10;

    private final JabatanStrukturRepository jabatanStrukturRepository;
    private final OpdRepository opdRepository;
    private final PenggunaRepository penggunaRepository;

    @Override
    @Transactional(readOnly = true)
    public GlobalSearchResultDto search(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return GlobalSearchResultDto.builder()
                    .personal(List.of())
                    .opd(List.of())
                    .user(List.of())
                    .build();
        }
        String kw = keyword.trim();

        List<JabatanStruktur> personals = jabatanStrukturRepository
                .findByNamaDummyContainingIgnoreCaseOrNipContainingIgnoreCaseOrUsernameContainingIgnoreCase(kw, kw, kw);
        List<Opd> opds = opdRepository.findByNamaOpdContainingIgnoreCase(kw);
        List<Pengguna> users = penggunaRepository
                .findByNamaContainingIgnoreCaseOrUsernameContainingIgnoreCase(kw, kw);

        return GlobalSearchResultDto.builder()
                .personal(personals.stream().limit(MAX_PER_KATEGORI)
                        .map(p -> GlobalSearchResultDto.PersonalResult.builder()
                                .id(p.getId())
                                .nama(p.getNamaDummy())
                                .nip(p.getNip())
                                .username(p.getUsername())
                                .jabatan(p.getJabatan().name())
                                .bidangId(p.getBidang().getId())
                                .build())
                        .toList())
                .opd(opds.stream().limit(MAX_PER_KATEGORI)
                        .map(o -> GlobalSearchResultDto.OpdResult.builder()
                                .id(o.getId())
                                .nama(o.getNamaOpd())
                                .build())
                        .toList())
                .user(users.stream().limit(MAX_PER_KATEGORI)
                        .map(u -> GlobalSearchResultDto.UserResult.builder()
                                .id(u.getId())
                                .nama(u.getNama())
                                .username(u.getUsername())
                                .role(u.getRole().name())
                                .build())
                        .toList())
                .build();
    }
}
