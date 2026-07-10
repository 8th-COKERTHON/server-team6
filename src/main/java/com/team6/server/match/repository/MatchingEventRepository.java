package com.team6.server.match.repository;

import com.team6.server.match.entity.MatchingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchingEventRepository extends JpaRepository<MatchingEvent, Long> {

    List<MatchingEvent> findByStatus(String status);

    Optional<MatchingEvent> findByEventTypeAndPeriodKey(String eventType, String periodKey);

    List<MatchingEvent> findByStatusAndEventTypeInOrderByStartsAtAsc(String status, List<String> eventTypes);

}
