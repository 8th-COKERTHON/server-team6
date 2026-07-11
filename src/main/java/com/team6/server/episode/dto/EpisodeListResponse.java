package com.team6.server.episode.dto;

import java.util.List;

public record EpisodeListResponse(List<EpisodeListItemResponse> items, String nextCursor, boolean hasNext) {}
