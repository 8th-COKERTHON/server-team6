package com.team6.server.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "matching_events") // 테이블명 싱크 맞추기
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatchingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType; // 'WEEKLY', 'MONTHLY', 'SPECIAL'

    @Column(name = "period_key", length = 20)
    private String periodKey;

    @Builder.Default
    @Column(name = "match_type", nullable = false, length = 30)
    private String matchType = "RIVAL";

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false, length = 20)
    private String status; // 'DRAFT', 'OPEN', 'CLOSED', 'CANCELLED'

    @Column(name = "score_reward", nullable = false)
    private Long scoreReward; // 기본값 0

    @Builder.Default
    @Column(name = "round_count", nullable = false)
    private Integer roundCount = 5;

    @Builder.Default
    @Column(name = "score_multiplier", nullable = false)
    private java.math.BigDecimal scoreMultiplier = java.math.BigDecimal.ONE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void close() {
        if ("OPEN".equals(status)) status = "CLOSED";
    }
}
