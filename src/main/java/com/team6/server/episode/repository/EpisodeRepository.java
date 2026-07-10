package com.team6.server.episode.repository;

import com.team6.server.episode.Episode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    long countByMemberIdAndStatus(Long memberId, Episode.Status status);
    Optional<Episode> findFirstByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
            Long memberId, LocalDateTime from, LocalDateTime to);

    @Query("""
            select m from Episode m
            where m.member.id = :memberId
              and (:status is null or m.status = :status)
              and (:cursorCreatedAt is null
                   or m.createdAt < :cursorCreatedAt
                   or (m.createdAt = :cursorCreatedAt and m.id < :cursorId))
            order by m.createdAt desc, m.id desc
            """)
    List<Episode> findPage(@Param("memberId") Long memberId,
                          @Param("status") Episode.Status status,
                          @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
                          @Param("cursorId") Long cursorId,
                          Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Episode e where e.member.id = :memberId and e.status = :status order by e.createdAt asc, e.id asc")
    List<Episode> findAvailableForUpdate(@Param("memberId") Long memberId,
                                         @Param("status") Episode.Status status,
                                         Pageable pageable);
}
