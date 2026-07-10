package com.team6.server.ranking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "episode_rankings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EpisodeRanking {

    @Id
    @Column(name = "episode_id")
    private Long episodeId;

    @Column(name = "title_score", nullable = false)
    private Long titleScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_title_id", nullable = true)
    private Title currentTitle;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addScore(int score) {
        this.titleScore += score;
    }

    public void updateTitle(Title newTitle) {
        this.currentTitle = newTitle;
    }
}