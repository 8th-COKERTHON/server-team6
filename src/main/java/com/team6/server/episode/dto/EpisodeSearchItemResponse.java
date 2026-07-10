package com.team6.server.episode.dto;

import java.time.LocalDate;

public record EpisodeSearchItemResponse(
        Long episodeId,
        String title,
        String contentPreview,
        LocalDate episodeDate,
        String status
) {}
