package com.team6.server.episode.service;

import org.springframework.stereotype.Component;

@Component
public class LocalTitleSuggestionProvider implements TitleSuggestionProvider {
    private static final int PREVIEW_LENGTH = 30;

    @Override
    public String suggest(String content) {
        String normalized = content.strip().replaceAll("\\s+", " ");
        int sentenceEnd = normalized.indexOf('.');
        String candidate = sentenceEnd > 0 ? normalized.substring(0, sentenceEnd) : normalized;
        return candidate.length() <= PREVIEW_LENGTH ? candidate : candidate.substring(0, PREVIEW_LENGTH).strip() + "…";
    }
}
