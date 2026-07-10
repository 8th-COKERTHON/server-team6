package com.team6.server.matching.repository;

import com.team6.server.matching.EpisodeMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface EpisodeMatchRepository extends JpaRepository<EpisodeMatch, Long> {
    boolean existsByMemberIdAndStatus(Long memberId, EpisodeMatch.Status status);
    List<EpisodeMatch> findBySessionIdOrderByRoundNoAsc(Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EpisodeMatch> findBySessionIdAndRoundNo(Long sessionId, Integer roundNo);
}
