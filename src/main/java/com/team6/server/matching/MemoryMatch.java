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
public class MemoryMatch extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id") private Long eventId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(name = "memory_a_id", nullable = false) private Long memoryAId;
    @Column(name = "memory_b_id", nullable = false) private Long memoryBId;
    @Column(name = "winner_memory_id") private Long winnerMemoryId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;

    public enum Status { IN_PROGRESS, COMPLETED, CANCELLED }
}
