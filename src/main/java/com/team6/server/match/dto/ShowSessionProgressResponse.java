package com.team6.server.match.dto;

public record ShowSessionProgressResponse(
        Long sessionId,
        String type,
        String status,
        int totalMatches,
        int completedMatches,
        NextMatch nextMatch
) {
    public record NextMatch(Long matchId, int matchOrder, EpisodeView episodeA, EpisodeView episodeB) {}
    public record EpisodeView(Long episodeId, String title, String content, java.time.LocalDate episodeDate,
                              long score, String titleName) {}
}
