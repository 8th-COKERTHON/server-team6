package com.team6.server.episode.service;

import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class EpisodeCursorCodec {
    public String encode(LocalDateTime createdAt, Long episodeId) {
        String value = createdAt + "|" + episodeId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public Cursor decode(String value) {
        if (value == null || value.isBlank()) return new Cursor(null, null);
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 2) throw new IllegalArgumentException();
            return new Cursor(LocalDateTime.parse(parts[0]), Long.valueOf(parts[1]));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "올바르지 않은 cursor입니다.");
        }
    }

    public record Cursor(LocalDateTime createdAt, Long episodeId) {}
}
