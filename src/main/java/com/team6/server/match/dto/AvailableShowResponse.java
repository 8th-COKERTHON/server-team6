package com.team6.server.match.dto;

import java.time.LocalDateTime;

public record AvailableShowResponse(Long showId, String type, String title, Long sessionId, String status,
                                    int matchCount, int completedMatchCount,
                                    LocalDateTime startsAt, LocalDateTime endsAt) {}
