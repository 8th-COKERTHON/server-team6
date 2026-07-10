package com.team6.server.ranking.dto;

import java.util.List;

public record RankingListResponse(List<RankingItemResponse> items, int page, int size,
                                  long totalElements, boolean hasNext) {}
