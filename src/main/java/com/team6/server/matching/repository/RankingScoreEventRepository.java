package com.team6.server.matching.repository;

import com.team6.server.matching.RankingScoreEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingScoreEventRepository extends JpaRepository<RankingScoreEvent, Long> { }
