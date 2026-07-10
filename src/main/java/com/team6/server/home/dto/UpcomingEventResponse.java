package com.team6.server.home.dto;

import java.time.OffsetDateTime;

public record UpcomingEventResponse(Long eventId, String type, String title, OffsetDateTime startsAt,
                                    OffsetDateTime endsAt, long daysRemaining, long scoreReward) {}
