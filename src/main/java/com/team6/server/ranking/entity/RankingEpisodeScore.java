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
public class RankingEpisodeScore {
    public static final long INITIAL_SCORE = 1000L;
    public static final long MIN_SCORE = 100L;

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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static RankingEpisodeScore initial(Long episodeId) {
        return RankingEpisodeScore.builder()
                .episodeId(episodeId)
                .titleScore(INITIAL_SCORE)
                .build();
    }

    public void applyDelta(long delta) {
        this.titleScore = Math.max(MIN_SCORE, this.titleScore + delta);
    }

    public void updateTitle(Title newTitle) {
        this.currentTitle = newTitle;
    }

    public Long getCurrentTitleId() {
        return currentTitle == null ? null : currentTitle.getId();
    }
}
