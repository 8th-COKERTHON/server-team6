package com.team6.server.matching.dto;

import java.time.LocalDateTime;

public record RingRoundResponse(Long matchId, int roundNo, int totalRounds, String status,
                                RingEpisodeResponse episodeA, RingEpisodeResponse episodeB,
                                Long winnerEpisodeId, LocalDateTime startedAt, LocalDateTime completedAt) { }
