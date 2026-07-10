package com.team6.server.match.dto;

import com.team6.server.match.entity.MatchingEvent;
import java.time.LocalDateTime;
import java.util.List;

public record RingResponse (
        Object activeQuestion, // 밸런스 질문
        List<AvailableMemoryDto> availableMemories, // 대결 가능한 내 기억들
        ActiveMatchDto activeMatch, // 진행 중인 대결
        List<ActiveEventDto> activeEvents // 진행할 수 있는 매칭 리스트
) {

    // 대결 가능한 본인 기억 요약
    public record AvailableMemoryDto(
            Long memoryId,
            String title,
            String memoryDate
    ) {}

    // 실데이터 매핑
    public record ActiveMatchDto(
            Long matchId,
            MemoryCardDto memoryA,
            MemoryCardDto memoryB,
            String status,         // 'IN_PROGRESS'
            int currentRound,      // 라운드 정보 (예: 1/5)
            int totalRounds        // 총 라운드 수
    ) {}

    // 카드 상세 정보
    public record MemoryCardDto(
            Long memoryId,
            String title,
            String content,
            String memoryDate
    ) {}

    // 링_목록 선택
    public record ActiveEventDto(
            Long eventId,
            String type,
            String title,
            String displayDate,
            Long scoreReward
    ) {
        public static ActiveEventDto from(MatchingEvent event) {
            if(event == null) return null;

            String formattedDate = String.format("%d.%d | %s",
                    event.getStartsAt().getMonthValue(),
                    event.getStartsAt().getDayOfMonth(),
                    event.getEventType());

            return new ActiveEventDto(
                    event.getId(),
                    event.getEventType(),
                    event.getTitle(),
                    formattedDate,
                    event.getScoreReward()
            );
        }
    }
}