package com.team6.server.home.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TodayMemoryResponse(Long memoryId, String title, LocalDate memoryDate, OffsetDateTime createdAt) {}
