package com.team6.server.matching.dto;

import jakarta.validation.constraints.NotNull;

public record StartRingSessionRequest(@NotNull Long eventId) { }
