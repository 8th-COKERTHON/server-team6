package com.team6.server.memory.dto;

import java.time.LocalDateTime;

public record CreateMemoryResponse(Long memoryId, String status, long titleScore, String currentTitle,
                                   long availableMemoryCount, boolean canStartMatch, LocalDateTime createdAt) {}
