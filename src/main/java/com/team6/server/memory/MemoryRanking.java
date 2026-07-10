package com.team6.server.memory;

import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "memory_rankings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoryRanking extends BaseTimeEntity {
    @Id @Column(name = "memory_id") private Long memoryId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @MapsId @JoinColumn(name = "memory_id") private Memory memory;
    @Column(name = "title_score", nullable = false) private long titleScore;
    @Column(name = "current_title_id") private Long currentTitleId;
    @Version @Column(nullable = false) private long version;

    public MemoryRanking(Memory memory) {
        this.memory = memory;
        this.titleScore = 0;
    }
}
