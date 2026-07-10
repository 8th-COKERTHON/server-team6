package com.team6.server.matching.dto;

import jakarta.validation.constraints.NotNull;

public record SelectRingWinnerRequest(@NotNull Long winnerEpisodeId) { }
