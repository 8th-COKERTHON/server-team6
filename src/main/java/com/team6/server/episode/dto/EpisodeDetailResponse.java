package com.team6.server.episode.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EpisodeDetailResponse(
        Long episodeId,
        Long memberId,
        String title,
        String content,
        LocalDate episodeDate,
        String status,
        LocalDateTime matchedAt,
        boolean rankingPresent,
        Long titleScore,
        Long currentTitleId,
        Long rankingVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
