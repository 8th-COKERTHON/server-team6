package com.team6.server.memory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TitleSuggestionRequest(@NotBlank @Size(max = 5000) String content) {}
