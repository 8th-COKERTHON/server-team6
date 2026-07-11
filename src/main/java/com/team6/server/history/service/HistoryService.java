package com.team6.server.history.service;

import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.history.dto.ChampionHistoryItemResponse;
import com.team6.server.history.dto.HistoryHomeResponse;
import com.team6.server.history.dto.MatchHistoryItemResponse;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {
    private static final int HOME_SECTION_SIZE = 2;

    private final CurrentMemberProvider currentMember;
    private final RankingEpisodeScoreRepository rankingEpisodeScoreRepository;
    private final MatchRepository matchRepository;

    public HistoryHomeResponse getHome(String query, Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        String keyword = normalize(query);
        return new HistoryHomeResponse(
                getChampionRecords(memberId, keyword, HOME_SECTION_SIZE),
                getMatchRecords(memberId, keyword, HOME_SECTION_SIZE)
        );
    }

    public List<ChampionHistoryItemResponse> getChampionRecords(String query, int size,
                                                                Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        return getChampionRecords(memberId, normalize(query), size);
    }

    public List<MatchHistoryItemResponse> getMatchRecords(String query, int size,
                                                          Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        return getMatchRecords(memberId, normalize(query), size);
    }

    private List<ChampionHistoryItemResponse> getChampionRecords(Long memberId, String keyword, int size) {
        return rankingEpisodeScoreRepository.findChampionHistory(
                memberId, keyword, PageRequest.of(0, size));
    }

    private List<MatchHistoryItemResponse> getMatchRecords(Long memberId, String keyword, int size) {
        return matchRepository.findCompletedMatchHistory(
                memberId, keyword, PageRequest.of(0, size));
    }

    private String normalize(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.strip().toLowerCase();
    }
}
