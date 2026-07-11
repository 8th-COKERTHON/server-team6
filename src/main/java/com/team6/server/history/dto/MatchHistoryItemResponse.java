package com.team6.server.history.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatchHistoryItemResponse(
        Long matchId,
        Long episodeAId,
        String episodeATitle,
        LocalDate episodeADate,
        String episodeAResult,
        Long episodeBId,
        String episodeBTitle,
        LocalDate episodeBDate,
        String episodeBResult,
        Long winnerEpisodeId,
        LocalDateTime completedAt
) {
}
