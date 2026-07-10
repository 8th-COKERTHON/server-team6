package com.team6.server.match.repository;

import com.team6.server.match.entity.MatchingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchingEventRepository extends JpaRepository<MatchingEvent, Long> {

    List<MatchingEvent> findByStatus(String status);

}
