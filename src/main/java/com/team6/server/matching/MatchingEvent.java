package com.team6.server.matching;

import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "matching_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingEvent extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 20) private Type eventType;
    @Column(nullable = false, length = 100) private String title;
    @Column(length = 1000) private String description;
    @Column(name = "starts_at", nullable = false) private LocalDateTime startsAt;
    @Column(name = "ends_at", nullable = false) private LocalDateTime endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "score_reward", nullable = false) private long scoreReward;
    @Column(name = "round_count", nullable = false) private int roundCount;

    public MatchingEvent(Type eventType, String title, LocalDateTime startsAt, LocalDateTime endsAt,
                         Status status, long scoreReward) {
        this(eventType, title, startsAt, endsAt, status, scoreReward, 5);
    }

    public MatchingEvent(Type eventType, String title, LocalDateTime startsAt, LocalDateTime endsAt,
                         Status status, long scoreReward, int roundCount) {
        this.eventType = eventType;
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.scoreReward = scoreReward;
        this.roundCount = roundCount;
    }

    public enum Type { WEEKLY, MONTHLY, SPECIAL }
    public enum Status { DRAFT, SCHEDULED, OPEN, CLOSED, CANCELLED }
}
