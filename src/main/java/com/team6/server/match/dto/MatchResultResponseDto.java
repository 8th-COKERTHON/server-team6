package com.team6.server.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class MatchResultResponseDto {

    private Long matchId;
    private String status;
    private Long winnerEpisodeId;
    private Long winnerEpisodeScoreAwarded;
    private Long winnerEpisodeTitleScore;
    private String winnerEpisodeTitle;
    private LocalDateTime completedAt;
}
