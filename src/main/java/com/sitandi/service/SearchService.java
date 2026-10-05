package com.sitandi.service;

import java.util.List;

import com.sitandi.dto.GlobalSearchResultDto;

public interface SearchService {
    GlobalSearchResultDto search(String keyword);
}
