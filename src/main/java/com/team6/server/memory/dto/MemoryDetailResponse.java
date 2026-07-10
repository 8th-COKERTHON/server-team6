package com.team6.server.memory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemoryDetailResponse(
        Long memoryId,
        Long memberId,
        String title,
        String content,
        LocalDate memoryDate,
        String status,
        LocalDateTime matchedAt,
        boolean rankingPresent,
        Long titleScore,
        Long currentTitleId,
        Long rankingVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
