package com.team6.server.matching;

import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "matches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeMatch extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id") private Long eventId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(name = "episode_a_id", nullable = false) private Long episodeAId;
    @Column(name = "episode_b_id", nullable = false) private Long episodeBId;
    @Column(name = "winner_episode_id") private Long winnerEpisodeId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    @Column(name = "session_id") private Long sessionId;
    @Column(name = "round_no") private Integer roundNo;

    public EpisodeMatch(Long eventId, Long memberId, Long episodeAId, Long episodeBId,
                        Long sessionId, int roundNo, LocalDateTime startedAt) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.episodeAId = episodeAId;
        this.episodeBId = episodeBId;
        this.sessionId = sessionId;
        this.roundNo = roundNo;
        this.status = Status.IN_PROGRESS;
        this.startedAt = startedAt;
    }

    public void complete(Long winnerEpisodeId, LocalDateTime completedAt) {
        if (status != Status.IN_PROGRESS) throw new IllegalStateException("Match is not in progress");
        if (!winnerEpisodeId.equals(episodeAId) && !winnerEpisodeId.equals(episodeBId)) {
            throw new IllegalArgumentException("Winner is not a match participant");
        }
        this.winnerEpisodeId = winnerEpisodeId;
        this.status = Status.COMPLETED;
        this.completedAt = completedAt;
    }

    public enum Status { IN_PROGRESS, COMPLETED, CANCELLED }
}
