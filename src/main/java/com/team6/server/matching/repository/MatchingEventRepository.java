package com.team6.server.matching.repository;

import com.team6.server.matching.MatchingEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingEventRepository extends JpaRepository<MatchingEvent, Long> {
    List<MatchingEvent> findByStatusAndStartsAtAfterOrderByStartsAtAscIdAsc(
            MatchingEvent.Status status, LocalDateTime now, Pageable pageable);

    List<MatchingEvent> findByStatusAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByStartsAtDescIdDesc(
            MatchingEvent.Status status, LocalDateTime startsAt, LocalDateTime endsAt);

    Optional<MatchingEvent> findByIdAndStatusAndStartsAtLessThanEqualAndEndsAtGreaterThan(
            Long id, MatchingEvent.Status status, LocalDateTime startsAt, LocalDateTime endsAt);
}
