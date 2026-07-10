package com.team6.server.matching.repository;

import com.team6.server.matching.RingSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RingSessionRepository extends JpaRepository<RingSession, Long> {
    Optional<RingSession> findByEventIdAndMemberId(Long eventId, Long memberId);
    Optional<RingSession> findByIdAndMemberId(Long id, Long memberId);
    boolean existsByMemberIdAndStatus(Long memberId, RingSession.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RingSession> findWithLockByIdAndMemberId(Long id, Long memberId);
}
