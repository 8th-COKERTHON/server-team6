package com.team6.server.home.service;

import com.team6.server.global.config.TimeConfig;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.home.dto.*;
import com.team6.server.matching.MatchingEvent;
import com.team6.server.matching.repository.MatchingEventRepository;
import com.team6.server.matching.EpisodeMatch;
import com.team6.server.matching.repository.EpisodeMatchRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HomeService {
    private final EpisodeRepository episodes;
    private final MatchingEventRepository events;
    private final CurrentMemberProvider currentMember;
    private final Clock clock;
    private final EpisodeMatchRepository matches;

    public HomeService(EpisodeRepository episodes, MatchingEventRepository events,
                       CurrentMemberProvider currentMember, Clock clock, EpisodeMatchRepository matches) {
        this.episodes = episodes;
        this.events = events;
        this.currentMember = currentMember;
        this.clock = clock;
        this.matches = matches;
    }

    public HomeResponse get(Authentication authentication) {
        var member = currentMember.require(authentication);
        var zone = TimeConfig.SERVICE_ZONE;
        var now = LocalDateTime.now(clock.withZone(zone));
        var today = now.toLocalDate();
        long count = episodes.countByMemberIdAndStatus(member.getId(), Episode.Status.AVAILABLE);
        var todayEpisode = episodes
                .findFirstByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
                        member.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .map(episode -> new TodayEpisodeResponse(episode.getId(), episode.getTitle(), episode.getEpisodeDate(),
                        episode.getCreatedAt().atZone(zone).toOffsetDateTime()))
                .orElse(null);
        var upcoming = events.findByStatusAndStartsAtAfterOrderByStartsAtAscIdAsc(
                        MatchingEvent.Status.SCHEDULED, now, PageRequest.of(0, 5)).stream()
                .map(event -> new UpcomingEventResponse(event.getId(), event.getEventType().name(), event.getTitle(),
                        event.getStartsAt().atZone(zone).toOffsetDateTime(),
                        event.getEndsAt().atZone(zone).toOffsetDateTime(),
                        ChronoUnit.DAYS.between(today, event.getStartsAt().toLocalDate()), event.getScoreReward()))
                .toList();
        boolean hasActiveMatch = matches.existsByMemberIdAndStatus(member.getId(), EpisodeMatch.Status.IN_PROGRESS);
        return new HomeResponse(today, count, count >= 2 && !hasActiveMatch, todayEpisode, upcoming);
    }
}
