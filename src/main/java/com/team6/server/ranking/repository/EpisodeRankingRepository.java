package com.team6.server.ranking.repository;

import com.team6.server.ranking.entity.EpisodeRanking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRankingRepository extends JpaRepository<EpisodeRanking, Long> {

}
