package com.team6.server.matching;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ranking_score_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingScoreEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_key", nullable = false, unique = true, length = 100) private String eventKey;
    @Column(name = "episode_id", nullable = false) private Long episodeId;
    @Column(name = "score_type", nullable = false, length = 20) private String scoreType;
    @Column(nullable = false) private long delta;
    @Column(name = "source_type", nullable = false, length = 30) private String sourceType;
    @Column(name = "source_id", nullable = false) private Long sourceId;
    @Column(name = "occurred_at", nullable = false) private LocalDateTime occurredAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    public RankingScoreEvent(Long matchId, Long episodeId, long delta, LocalDateTime now) {
        this.eventKey = "MATCH:" + matchId + ":TITLE";
        this.episodeId = episodeId;
        this.scoreType = "TITLE";
        this.delta = delta;
        this.sourceType = "MATCH";
        this.sourceId = matchId;
        this.occurredAt = now;
        this.createdAt = now;
    }
}
