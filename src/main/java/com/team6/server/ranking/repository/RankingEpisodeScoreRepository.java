package com.team6.server.ranking.repository;

import com.team6.server.history.dto.ChampionHistoryItemResponse;
import com.team6.server.ranking.entity.RankingEpisodeScore;
import java.time.LocalDateTime;
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
    List<ChampionHistoryItemResponse> findChampionHistory(
            @Param("memberId") Long memberId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query(value = """
            SELECT er.episode_id AS episodeId, e.title AS episodeTitle,
                   er.title_score AS score, t.name AS titleName,
                   (SELECT COUNT(*) + 1 FROM episode_rankings higher
                    WHERE higher.title_score > er.title_score) AS competitionRank
            FROM episode_rankings er
            JOIN episodes e ON e.id = er.episode_id
            LEFT JOIN titles t ON t.id = er.current_title_id
            ORDER BY er.title_score DESC, er.episode_id ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<RankingRow> findRankingPage(
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = "SELECT COUNT(*) FROM episode_rankings", nativeQuery = true)
    long countRankings();

    @Query(value = """
            SELECT er.episode_id AS episodeId, e.title AS episodeTitle,
                   er.title_score AS score, t.name AS titleName
            FROM episode_rankings er
            JOIN episodes e ON e.id = er.episode_id
            LEFT JOIN titles t ON t.id = er.current_title_id
            WHERE e.member_id = :memberId
            ORDER BY er.title_score DESC, er.episode_id ASC
            LIMIT 1
            """, nativeQuery = true)
    List<RankingRow> findAllTimeChampion(@Param("memberId") Long memberId);

    @Query(value = """
            SELECT rse.episode_id AS episodeId, e.title AS episodeTitle,
                   SUM(rse.delta) AS score, t.name AS titleName
            FROM ranking_score_events rse
            JOIN episodes e ON e.id = rse.episode_id
            JOIN episode_rankings er ON er.episode_id = rse.episode_id
            LEFT JOIN titles t ON t.id = er.current_title_id
            WHERE e.member_id = :memberId
              AND rse.score_type = 'TITLE'
              AND rse.occurred_at >= :from
              AND rse.occurred_at < :to
            GROUP BY rse.episode_id, e.title, er.title_score, t.name
            ORDER BY SUM(rse.delta) DESC, er.title_score DESC, rse.episode_id ASC
            LIMIT 1
            """, nativeQuery = true)
    List<RankingRow> findPeriodChampion(
            @Param("memberId") Long memberId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    interface RankingRow {
        Long getEpisodeId();

        String getEpisodeTitle();

        Long getScore();

        String getTitleName();

        Long getCompetitionRank();
    }
}