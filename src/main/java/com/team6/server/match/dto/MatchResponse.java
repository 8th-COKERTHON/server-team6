package com.team6.server.match.dto;

import com.team6.server.match.entity.MatchingEvent;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatchResponse (

        Long id,
        String eventType,
        String title,
        String description,
        LocalDateTime applicationOpensAt,
        LocalDateTime applicationClosesAt,
        LocalDateTime startsAt,
        Integer capacity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) {

    public static MatchResponse from(MatchingEvent event) {

        return new MatchResponse(
                event.getId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getApplicationOpensAt(),
                event.getApplicationClosesAt(),
                event.getStartsAt(),
                event.getCapacity(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
