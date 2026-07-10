package com.team6.server.ranking.dto;

public record RankingItemResponse(Long episodeId, String episodeTitle, long score, long rank,
                                  String title) {}
