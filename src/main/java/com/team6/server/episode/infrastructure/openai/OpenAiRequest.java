package com.team6.server.episode.infrastructure.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

record OpenAiRequest(
        String model,
        String instructions,
        String input,
        @JsonProperty("max_output_tokens") int maxOutputTokens
) {}
