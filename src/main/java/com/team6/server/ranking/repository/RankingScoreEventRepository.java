package com.team6.server.ranking.repository;

import com.team6.server.ranking.entity.RankingScoreEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingScoreEventRepository extends JpaRepository<RankingScoreEvent, Long> {
    boolean existsByEventKey(String eventKey);
}
