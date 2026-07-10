package com.team6.server.episode.repository;


import com.team6.server.episode.entity.Episode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Episode e where e.id in :episodeIds order by e.id asc")
    List<Episode> findAllByIdWithPessimisticLock(@Param("episodeIds") List<Long> episodeIds);

    List<Episode> findAllByMemberIdAndStatus(Long memberId, String status);
}
