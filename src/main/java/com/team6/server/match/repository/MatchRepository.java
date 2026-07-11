package com.team6.server.match.repository;

import com.team6.server.history.dto.MatchHistoryItemResponse;
import com.team6.server.match.entity.Match;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByMatchingEventId(Long eventId);

    long countBySessionIdAndStatus(Long sessionId, String status);

    List<Match> findBySessionIdOrderByMatchOrderAsc(Long sessionId);

    Optional<Match> findFirstBySessionIdAndStatusOrderByMatchOrderAsc(Long sessionId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> findByIdWithPessimisticLock(Long id);

    @Query("""
            select new com.team6.server.history.dto.MatchHistoryItemResponse(
                m.id,
                episodeA.id,
                episodeA.title,
                episodeA.episodeDate,
                case when m.winnerEpisodeId = episodeA.id then 'WIN' else 'LOSS' end,
                episodeB.id,
                episodeB.title,
                episodeB.episodeDate,
                case when m.winnerEpisodeId = episodeB.id then 'WIN' else 'LOSS' end,
                m.winnerEpisodeId,
                m.completedAt
            )
            from Match m
            join Episode episodeA on episodeA.id = m.episodeAId
            join Episode episodeB on episodeB.id = m.episodeBId
            where m.memberId = :memberId
              and m.status = 'COMPLETED'
              and (:query is null
                   or lower(episodeA.title) like concat('%', :query, '%')
                   or lower(episodeA.content) like concat('%', :query, '%')
                   or lower(episodeB.title) like concat('%', :query, '%')
                   or lower(episodeB.content) like concat('%', :query, '%'))
            order by m.completedAt desc, m.id desc
            """)
    List<MatchHistoryItemResponse> findCompletedMatchHistory(@Param("memberId") Long memberId,
                                                             @Param("query") String query,
                                                             Pageable pageable);

}
