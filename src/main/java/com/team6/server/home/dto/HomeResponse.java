package com.team6.server.home.dto;

import java.time.LocalDate;
import java.util.List;

public record HomeResponse(LocalDate today, long availableEpisodeCount, boolean canStartMatch,
                           TodayEpisodeResponse todayEpisode, List<UpcomingEventResponse> upcomingEvents) {}
