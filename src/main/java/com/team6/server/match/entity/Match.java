package com.team6.server.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = true)
    private MatchingEvent matchingEvent;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "episode_a_id", nullable = false)
    private Long episodeAId;

    @Column(name = "episode_b_id", nullable = false)
    private Long episodeBId;

    @Column(name = "winner_episode_id")
    private Long winnerEpisodeId;

    @Column(name = "loser_episode_id")
    private Long loserEpisodeId;

    @Builder.Default
    @Column(name = "match_type", nullable = false, length = 30)
    private String matchType = "GENERAL";

    @Column(name = "match_order")
    private Integer matchOrder;

    @Column(name = "episode_a_score_before")
    private Long episodeAScoreBefore;

    @Column(name = "episode_b_score_before")
    private Long episodeBScoreBefore;

    @Column(name = "episode_a_score_after")
    private Long episodeAScoreAfter;

    @Column(name = "episode_b_score_after")
    private Long episodeBScoreAfter;

    @Column(name = "winner_delta")
    private Long winnerDelta;

    @Column(name = "loser_delta")
    private Long loserDelta;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void complete(Long winnerEpisodeId, Long loserEpisodeId, long episodeAScoreBefore,
                         long episodeBScoreBefore, long episodeAScoreAfter, long episodeBScoreAfter,
                         long winnerDelta, long loserDelta, LocalDateTime completedAt) {
        this.status = "COMPLETED";
        this.winnerEpisodeId = winnerEpisodeId;
        this.loserEpisodeId = loserEpisodeId;
        this.episodeAScoreBefore = episodeAScoreBefore;
        this.episodeBScoreBefore = episodeBScoreBefore;
        this.episodeAScoreAfter = episodeAScoreAfter;
        this.episodeBScoreAfter = episodeBScoreAfter;
        this.winnerDelta = winnerDelta;
        this.loserDelta = loserDelta;
        this.completedAt = completedAt;
    }
}
