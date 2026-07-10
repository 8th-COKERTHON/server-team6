package com.team6.server.ranking.service;

import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.ranking.dto.RankingItemResponse;
import com.team6.server.ranking.dto.RankingListResponse;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingService {
    private final CurrentMemberProvider currentMember;
    private final RankingEpisodeScoreRepository rankings;

    public RankingService(CurrentMemberProvider currentMember, RankingEpisodeScoreRepository rankings) {
        this.currentMember = currentMember;
        this.rankings = rankings;
    }

    @Transactional(readOnly = true)
    public RankingListResponse getAll(int page, int size, Authentication authentication) {
        currentMember.require(authentication);
        long offset = (long) page * size;
        var items = rankings.findRankingPage(size, offset).stream()
                .map(row -> new RankingItemResponse(row.getEpisodeId(), row.getEpisodeTitle(), row.getScore(),
                        row.getCompetitionRank(), row.getTitleName()))
                .toList();
        long total = rankings.countRankings();
        return new RankingListResponse(items, page, size, total, offset + items.size() < total);
    }
}
