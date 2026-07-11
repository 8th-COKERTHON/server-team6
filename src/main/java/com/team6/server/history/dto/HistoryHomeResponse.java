package com.team6.server.history.dto;

import java.util.List;

public record HistoryHomeResponse(
        List<ChampionHistoryItemResponse> championRecords,
        List<MatchHistoryItemResponse> matchRecords
) {
}
