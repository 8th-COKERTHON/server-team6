package com.team6.server.member.dto;

public record OnboardingStatusResponse(
        String onboardingStatus,
        Long activePlacementSessionId,
        long registeredEpisodeCount,
        long completedPlacementMatches,
        long totalPlacementMatches
) {}
