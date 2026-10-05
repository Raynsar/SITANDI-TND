package com.sitandi.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.dto.GlobalSearchResultDto;
import com.sitandi.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchApiController {

    private final SearchService searchService;

    /**
     * Global search: mencari personal, OPD, dan user sekaligus.
     * GET /api/v1/search?q=keyword
     */
    @GetMapping
    public GlobalSearchResultDto search(@RequestParam("q") String q) {
        return searchService.search(q);
    }
}
