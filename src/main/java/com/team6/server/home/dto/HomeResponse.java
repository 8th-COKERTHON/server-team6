package com.team6.server.home.dto;

import java.time.LocalDate;
import java.util.List;

public record HomeResponse(LocalDate today, long availableMemoryCount, boolean canStartMatch,
                           TodayMemoryResponse todayMemory, List<UpcomingEventResponse> upcomingEvents) {}
