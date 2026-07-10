package com.team6.server.episode;

import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "episode_rankings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeRanking extends BaseTimeEntity {
    @Id @Column(name = "episode_id") private Long episodeId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @MapsId @JoinColumn(name = "episode_id") private Episode episode;
    @Column(name = "title_score", nullable = false) private long titleScore;
    @Column(name = "current_title_id") private Long currentTitleId;
    @Version @Column(nullable = false) private long version;

    public EpisodeRanking(Episode episode) {
        this.episode = episode;
        this.titleScore = 0;
    }

    public void award(long score) {
        if (score < 0) throw new IllegalArgumentException("Score must not be negative");
        this.titleScore += score;
    }
}
