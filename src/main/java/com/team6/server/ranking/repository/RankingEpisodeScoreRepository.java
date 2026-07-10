package com.team6.server.ranking.repository;

import com.team6.server.history.dto.ChampionHistoryItemResponse;
import com.team6.server.ranking.entity.RankingEpisodeScore;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingEpisodeScoreRepository extends JpaRepository<RankingEpisodeScore, Long> {
    @Query("""
            select new com.team6.server.history.dto.ChampionHistoryItemResponse(
                e.id,
                e.title,
                r.titleScore,
                '올타임 챔피언 (All-Time Champion)',
                e.episodeDate,
                r.updatedAt
            )
            from RankingEpisodeScore r
            join Episode e on e.id = r.episodeId
            where e.member.id = :memberId
              and (:query is null
                   or lower(e.title) like concat('%', :query, '%')
                   or lower(e.content) like concat('%', :query, '%'))
            order by r.titleScore desc, r.updatedAt desc, e.id desc
            """)
    List<ChampionHistoryItemResponse> findChampionHistory(@Param("memberId") Long memberId,
                                                          @Param("query") String query,
                                                          Pageable pageable);
}
