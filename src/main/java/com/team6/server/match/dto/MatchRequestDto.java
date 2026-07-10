package com.team6.server.match.dto;

import jakarta.validation.constraints.NotNull;

public record MatchRequestDto(
        @NotNull Long episodeAId,
        @NotNull Long episodeBId
) {}
