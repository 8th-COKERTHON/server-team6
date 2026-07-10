package com.team6.server.match.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ring_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id") private Long eventId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(name = "session_type", nullable = false, length = 30) private String sessionType;
    @Column(name = "primary_episode_id") private Long primaryEpisodeId;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "total_rounds", nullable = false) private Integer totalRounds;
    @Column(name = "completed_rounds", nullable = false) private Integer completedRounds;
    @Column(name = "score_multiplier", nullable = false) private BigDecimal scoreMultiplier;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    @Builder
    public ShowSession(Long eventId, Long memberId, String sessionType, Long primaryEpisodeId, int totalRounds,
                       BigDecimal scoreMultiplier, LocalDateTime startedAt) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.sessionType = sessionType;
        this.primaryEpisodeId = primaryEpisodeId;
        this.status = "IN_PROGRESS";
        this.totalRounds = totalRounds;
        this.completedRounds = 0;
        this.scoreMultiplier = scoreMultiplier;
        this.startedAt = startedAt;
    }

    public boolean completeRound(LocalDateTime now) {
        if (!"IN_PROGRESS".equals(status) || completedRounds >= totalRounds) {
            throw new IllegalStateException("Show session cannot progress");
        }
        completedRounds++;
        if (completedRounds.equals(totalRounds)) {
            status = "COMPLETED";
            completedAt = now;
            return true;
        }
        return false;
    }
}
