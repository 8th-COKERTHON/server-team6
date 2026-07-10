package com.team6.server.matching.dto;

public record RingRoundResultResponse(Long sessionId, String sessionStatus, int completedRounds,
                                      int totalRounds, Long winnerEpisodeId, long scoreAwarded,
                                      long winnerTitleScore, RingRoundResponse nextRound) { }
