package com.team6.server.match.repository;

import com.team6.server.match.entity.ShowSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSessionRepository extends JpaRepository<ShowSession, Long> {
    Optional<ShowSession> findByEventIdAndMemberId(Long eventId, Long memberId);

    boolean existsByMemberIdAndSessionTypeAndStatus(Long memberId, String sessionType, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowSession s where s.id = :id")
    Optional<ShowSession> findByIdWithPessimisticLock(@Param("id") Long id);

    Optional<ShowSession> findFirstByMemberIdAndSessionTypeAndStatusOrderByStartedAtDesc(
            Long memberId, String sessionType, String status);

    Optional<ShowSession> findFirstByMemberIdAndSessionTypeOrderByStartedAtDesc(Long memberId, String sessionType);
}
