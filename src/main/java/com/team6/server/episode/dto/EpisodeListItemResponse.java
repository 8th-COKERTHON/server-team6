package com.team6.server.episode.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EpisodeListItemResponse(
        Long episodeId,
        String title,
        String contentPreview,
        LocalDate episodeDate,
        String status,
        boolean rankingPresent,
        Long titleScore,
        Long currentTitleId,
        LocalDateTime matchedAt,
        LocalDateTime createdAt
) {}
