package com.team6.server.episode.infrastructure.openai;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("openai.title-suggestion")
public record OpenAiProperties(
        boolean enabled,
        String apiKey,
        String baseUrl,
        String model,
        Duration connectTimeout,
        Duration readTimeout,
        int maxOutputTokens,
        int maxTitleLength
) {
    public void validateForEnabledProvider() {
        requireText(apiKey, "OPENAI_API_KEY");
        requireText(baseUrl, "OPENAI_BASE_URL");
        requireText(model, "OPENAI_TITLE_MODEL");
        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
            throw new IllegalStateException("OPENAI_CONNECT_TIMEOUT must be positive");
        }
        if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            throw new IllegalStateException("OPENAI_READ_TIMEOUT must be positive");
        }
        if (maxOutputTokens <= 0 || maxTitleLength <= 0) {
            throw new IllegalStateException("OpenAI output limits must be positive");
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required when OpenAI title suggestion is enabled");
        }
    }
}
