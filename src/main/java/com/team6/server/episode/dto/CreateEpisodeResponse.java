package com.team6.server.episode.dto;

import java.time.LocalDateTime;

public record CreateEpisodeResponse(Long episodeId, String status, long titleScore, String currentTitle,
                                   long availableEpisodeCount, boolean canStartMatch, LocalDateTime createdAt) {}
