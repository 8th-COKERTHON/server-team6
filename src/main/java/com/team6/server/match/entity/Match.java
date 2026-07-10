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
    @JoinColumn(name = "event_id", nullable = true) // Nullable 허용 반영
    private MatchingEvent matchingEvent;

    // 테이블 무결성 규칙상 회원 테이블 및 메모리 소유권과 엄격하게 묶여있으므로 Long ID로 안전하게 들고 감
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "memory_a_id", nullable = false)
    private Long memoryAId;

    @Column(name = "memory_b_id", nullable = false)
    private Long memoryBId;

    @Column(name = "winner_memory_id")
    private Long winnerMemoryId;

    @Column(nullable = false, length = 20)
    private String status; // 'IN_PROGRESS', 'COMPLETED', 'CANCELLED' (기본값 'IN_PROGRESS')

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
}