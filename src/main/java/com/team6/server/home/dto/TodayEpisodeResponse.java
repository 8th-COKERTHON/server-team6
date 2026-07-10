package com.team6.server.home.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TodayEpisodeResponse(Long episodeId, String title, LocalDate episodeDate, OffsetDateTime createdAt) {}
