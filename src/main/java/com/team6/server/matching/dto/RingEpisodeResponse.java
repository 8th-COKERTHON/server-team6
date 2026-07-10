package com.team6.server.matching.dto;

import java.time.LocalDate;

public record RingEpisodeResponse(Long episodeId, String title, String content, LocalDate episodeDate) { }
