package com.team6.server.matching.dto;

import java.time.LocalDateTime;

public record RingEventResponse(Long eventId, String type, String title, String description,
                                LocalDateTime startsAt, LocalDateTime endsAt, long scoreReward,
                                int roundCount, String participationStatus, Long sessionId) { }
