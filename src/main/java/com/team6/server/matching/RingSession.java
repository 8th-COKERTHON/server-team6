package com.team6.server.matching;

import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ring_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RingSession extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id", nullable = false) private Long eventId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "total_rounds", nullable = false) private int totalRounds;
    @Column(name = "completed_rounds", nullable = false) private int completedRounds;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    public RingSession(Long eventId, Long memberId, int totalRounds, LocalDateTime startedAt) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.totalRounds = totalRounds;
        this.completedRounds = 0;
        this.status = Status.IN_PROGRESS;
        this.startedAt = startedAt;
    }

    public void completeRound(LocalDateTime now) {
        if (status != Status.IN_PROGRESS || completedRounds >= totalRounds) {
            throw new IllegalStateException("Ring session cannot advance");
        }
        completedRounds++;
        if (completedRounds == totalRounds) {
            status = Status.COMPLETED;
            completedAt = now;
        }
    }

    public enum Status { IN_PROGRESS, COMPLETED }
}
