package com.team6.server.episode.dto;

import java.util.List;

public record EpisodeSearchResponse(
        String query,
        String matchedBy,
        List<EpisodeSearchItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}
