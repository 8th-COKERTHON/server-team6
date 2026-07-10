package com.team6.server.ranking.repository;

import com.team6.server.ranking.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TitleRepository extends JpaRepository<Title, Long> {
    List<Title> findAllByOrderByMinScoreAsc();
}
