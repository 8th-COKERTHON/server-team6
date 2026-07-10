package com.team6.server.matching.repository;

import com.team6.server.matching.MemoryMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryMatchRepository extends JpaRepository<MemoryMatch, Long> {
    boolean existsByMemberIdAndStatus(Long memberId, MemoryMatch.Status status);
}
