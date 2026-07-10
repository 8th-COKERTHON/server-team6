package com.team6.server.episode;

import com.team6.server.global.entity.BaseTimeEntity;
import com.team6.server.member.Member;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "episodes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Column(nullable = false, length = 150) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "episode_date", nullable = false) private LocalDate episodeDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "matched_at") private LocalDateTime matchedAt;

    public Episode(Member member, String title, String content, LocalDate episodeDate) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.episodeDate = episodeDate;
        this.status = Status.AVAILABLE;
    }

    public void markMatched(LocalDateTime matchedAt) {
        if (status != Status.AVAILABLE) throw new IllegalStateException("Episode is not available");
        this.status = Status.MATCHED;
        this.matchedAt = matchedAt;
    }

    public enum Status { AVAILABLE, MATCHED, ARCHIVED }
}
