package com.team6.server.episode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateEpisodeRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 5000) String content,
        @NotNull LocalDate episodeDate
) {}
