package com.team6.server.match.repository;

import com.team6.server.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByMatchingEventId(Long eventId);

}