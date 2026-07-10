package com.team6.server.memory;

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
@Table(name = "memories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memory extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Column(nullable = false, length = 150) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "memory_date", nullable = false) private LocalDate memoryDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "matched_at") private LocalDateTime matchedAt;

    public Memory(Member member, String title, String content, LocalDate memoryDate) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.memoryDate = memoryDate;
        this.status = Status.AVAILABLE;
    }

    public enum Status { AVAILABLE, MATCHED, ARCHIVED }
}
