package com.team6.server.memory.repository;

import com.team6.server.memory.Memory;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
    long countByMemberIdAndStatus(Long memberId, Memory.Status status);
    Optional<Memory> findFirstByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
            Long memberId, LocalDateTime from, LocalDateTime to);
}
