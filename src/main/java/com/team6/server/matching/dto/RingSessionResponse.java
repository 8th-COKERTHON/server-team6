package com.team6.server.matching.dto;

import java.time.LocalDateTime;

public record RingSessionResponse(Long sessionId, Long eventId, String eventTitle, String status,
                                  int totalRounds, int completedRounds, RingRoundResponse currentRound,
                                  long totalScoreAwarded, LocalDateTime startedAt, LocalDateTime completedAt) { }
