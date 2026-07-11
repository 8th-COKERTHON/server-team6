package com.team6.server.episode.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "openai.title-suggestion", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalTitleSuggestionProvider implements TitleSuggestionProvider {
    private static final int PREVIEW_LENGTH = 30;
    private static final String LEADING_TIME_EXPRESSION = "^(오늘|어제|그제|지난날)\\s+";
    private static final String SENTENCE_ENDING =
            "(?:을|를)?\\s*(?:했|하였|됐|되었|이었|였|했었|있었|없었)(?:습니다|어요|다)$";

    @Override
    public String suggest(String content) {
        String normalized = content.strip().replaceAll("\\s+", " ");
        String firstSentence = normalized.split("(?:[.!?。！？]|\\R)", 2)[0].strip();
        String candidate = firstSentence
                .replaceFirst(LEADING_TIME_EXPRESSION, "")
                .replaceFirst(SENTENCE_ENDING, "")
                .replaceAll("[\\s,，:：;；]+$", "")
                .strip();
        if (candidate.isBlank()) {
            candidate = firstSentence;
        }
        return candidate.length() <= PREVIEW_LENGTH ? candidate : candidate.substring(0, PREVIEW_LENGTH).strip() + "…";
    }
}
