package com.team6.server.home.service;

import com.team6.server.global.config.TimeConfig;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.home.dto.*;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HomeService {
    private final EpisodeRepository episodes;
    private final CurrentMemberProvider currentMember;
    private final Clock clock;

    public HomeService(EpisodeRepository episodes, CurrentMemberProvider currentMember, Clock clock) {
        this.episodes = episodes;
        this.currentMember = currentMember;
        this.clock = clock;
    }

    public HomeResponse get(Authentication authentication) {
        var member = currentMember.require(authentication);
        var zone = TimeConfig.SERVICE_ZONE;
        var today = LocalDate.now(clock.withZone(zone));
        long count = episodes.countByMemberIdAndStatus(member.getId(), Episode.Status.AVAILABLE);
        var todayEpisode = episodes
                .findFirstByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
                        member.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .map(episode -> new TodayEpisodeResponse(episode.getId(), episode.getTitle(), episode.getEpisodeDate(),
                        episode.getCreatedAt().atZone(zone).toOffsetDateTime()))
                .orElse(null);
        return new HomeResponse(today, count, false, todayEpisode, List.of());
    }
}
