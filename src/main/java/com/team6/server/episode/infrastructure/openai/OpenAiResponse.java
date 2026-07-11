package com.team6.server.episode.infrastructure.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiResponse(String id, String status, IncompleteDetails incompleteDetails, List<Output> output) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record IncompleteDetails(String reason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Output(List<Content> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(String type, String text) {}
}
