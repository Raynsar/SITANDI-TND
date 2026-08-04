package com.sitandi.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk rendering tree struktur organisasi.
 * Struktur nested: id, nama, tipe, children[].
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisasiTreeNodeDto {

    private Long id;
    private String nama;
    private String tipe;

    @Builder.Default
    private List<OrganisasiTreeNodeDto> children = new ArrayList<>();
}

