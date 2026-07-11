package com.team6.server.history.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChampionHistoryItemResponse(
        Long episodeId,
        String episodeTitle,
        Long titleScore,
        String championTitle,
        LocalDate episodeDate,
        LocalDateTime achievedAt
) {
}
