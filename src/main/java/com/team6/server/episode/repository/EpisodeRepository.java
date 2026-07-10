package com.team6.server.episode.repository;

import com.team6.server.episode.Episode;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    long countByMemberId(Long memberId);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Episode e where e.member.id = :memberId and e.status = :status and e.placementStatus = :placementStatus order by e.createdAt asc, e.id asc")
    List<Episode> findReadyForShowForUpdate(@Param("memberId") Long memberId,
                                            @Param("status") Episode.Status status,
                                            @Param("placementStatus") Episode.PlacementStatus placementStatus,
                                            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Episode e where e.member.id = :memberId and e.status = :status and e.placementStatus = :placementStatus order by e.createdAt asc, e.id asc")
    List<Episode> findOnboardingCandidatesForUpdate(@Param("memberId") Long memberId,
                                                    @Param("status") Episode.Status status,
                                                    @Param("placementStatus") Episode.PlacementStatus placementStatus,
                                                    Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e from Episode e join RankingEpisodeScore r on r.episodeId = e.id
            where e.member.id = :memberId
              and e.id <> :targetEpisodeId
              and e.status = :status
              and e.placementStatus = :placementStatus
            order by abs(r.titleScore - :targetScore), e.id
            """)
    List<Episode> findPlacementOpponentsForUpdate(@Param("memberId") Long memberId,
                                                  @Param("targetEpisodeId") Long targetEpisodeId,
                                                  @Param("targetScore") long targetScore,
                                                  @Param("status") Episode.Status status,
                                                  @Param("placementStatus") Episode.PlacementStatus placementStatus,
                                                  Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Episode e where e.id in :episodeIds order by e.id asc")
    List<Episode> findAllByIdWithPessimisticLock(@Param("episodeIds") List<Long> episodeIds);

    List<Episode> findAllByMemberIdAndStatusOrderByCreatedAtDescIdDesc(Long memberId, Episode.Status status);

    boolean existsByMemberIdAndTitleContainingIgnoreCase(Long memberId, String query);

    Page<Episode> findByMemberIdAndTitleContainingIgnoreCaseOrderByCreatedAtDescIdDesc(
            Long memberId, String query, Pageable pageable);

    Page<Episode> findByMemberIdAndContentContainingIgnoreCaseOrderByCreatedAtDescIdDesc(
            Long memberId, String query, Pageable pageable);
}
