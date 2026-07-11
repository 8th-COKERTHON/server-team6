package com.team6.server.match.service;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.match.dto.AvailableShowResponse;
import com.team6.server.match.dto.ShowSessionResponse;
import com.team6.server.match.dto.ShowSessionProgressResponse;
import com.team6.server.match.entity.MatchingEvent;
import com.team6.server.match.entity.ShowSession;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.match.repository.MatchingEventRepository;
import com.team6.server.match.repository.ShowSessionRepository;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {
    private final CurrentMemberProvider currentMember;
    private final MemberRepository members;
    private final EpisodeRepository episodes;
    private final MatchingEventRepository events;
    private final ShowSessionRepository sessions;
    private final MatchRepository matches;
    private final RankingEpisodeScoreRepository rankings;
    private final BalancedPairingPolicy pairingPolicy;
    private final Clock clock;

    public ShowService(CurrentMemberProvider currentMember, MemberRepository members, EpisodeRepository episodes,
                       MatchingEventRepository events, ShowSessionRepository sessions,
                       MatchRepository matches, RankingEpisodeScoreRepository rankings,
                       BalancedPairingPolicy pairingPolicy, Clock clock) {
        this.currentMember = currentMember; this.members = members; this.episodes = episodes;
        this.events = events; this.sessions = sessions; this.matches = matches; this.rankings = rankings;
        this.pairingPolicy = pairingPolicy; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AvailableShowResponse> available(Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        return events.findByStatusAndEventTypeInOrderByStartsAtAsc("OPEN", List.of("WEEKLY", "MONTHLY"))
                .stream().map(event -> {
                    ShowSession session = sessions.findByEventIdAndMemberId(event.getId(), memberId).orElse(null);
                    return new AvailableShowResponse(event.getId(), event.getEventType(), event.getTitle(),
                            session == null ? null : session.getId(), session == null ? "READY" : session.getStatus(),
                            event.getRoundCount(), session == null ? 0 : session.getCompletedRounds(),
                            event.getStartsAt(), event.getEndsAt());
                }).toList();
    }

    @Transactional
    public ShowSessionResponse start(Long showId, Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        members.findWithLockById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MatchingEvent event = events.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKLY_SHOW_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        if (!"OPEN".equals(event.getStatus()) || now.isBefore(event.getStartsAt()) || !now.isBefore(event.getEndsAt())) {
            throw new BusinessException(ErrorCode.WEEKLY_SHOW_NOT_AVAILABLE);
        }
        if (sessions.findByEventIdAndMemberId(showId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.RING_SESSION_ALREADY_STARTED);
        }
        int episodeLimit = Math.max(2, event.getRoundCount() + 1);
        List<Episode> selected = episodes.findReadyForShowForUpdate(memberId, Episode.Status.AVAILABLE,
                Episode.PlacementStatus.COMPLETED, org.springframework.data.domain.PageRequest.of(0, episodeLimit));
        if (selected.size() < 2) throw new BusinessException(ErrorCode.INSUFFICIENT_RING_EPISODES);
        var pairs = pairingPolicy.create(selected, event.getRoundCount());
        int rounds = pairs.size();
        ShowSession session = sessions.save(ShowSession.builder().eventId(showId).memberId(memberId)
                .sessionType(event.getEventType()).totalRounds(rounds)
                .scoreMultiplier(event.getScoreMultiplier()).startedAt(now).build());
        List<com.team6.server.match.entity.Match> generated = new ArrayList<>();
        int order = 1;
        for (var pair : pairs) {
            generated.add(PlacementService.newMatch(session.getId(), memberId, pair.a().getId(),
                    pair.b().getId(), event.getMatchType(), order++, now));
        }
        matches.saveAll(generated);
        return new ShowSessionResponse(session.getId(), session.getSessionType(), session.getStatus(),
                session.getTotalRounds(), session.getCompletedRounds());
    }

    @Transactional(readOnly = true)
    public ShowSessionProgressResponse getSession(Long sessionId, Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        ShowSession session = sessions.findById(sessionId)
                .filter(item -> item.getMemberId().equals(memberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND));
        var next = matches.findFirstBySessionIdAndStatusOrderByMatchOrderAsc(sessionId, "IN_PROGRESS")
                .map(match -> new ShowSessionProgressResponse.NextMatch(match.getId(), match.getMatchOrder(),
                        episodeView(match.getEpisodeAId()), episodeView(match.getEpisodeBId()))).orElse(null);
        return new ShowSessionProgressResponse(session.getId(), session.getSessionType(), session.getStatus(),
                session.getTotalRounds(), session.getCompletedRounds(), next);
    }

    private ShowSessionProgressResponse.EpisodeView episodeView(Long episodeId) {
        Episode episode = episodes.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
        var ranking = rankings.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
        return new ShowSessionProgressResponse.EpisodeView(episode.getId(), episode.getTitle(), episode.getContent(),
                episode.getEpisodeDate(), ranking.getTitleScore(),
                ranking.getCurrentTitle() == null ? null : ranking.getCurrentTitle().getName());
    }
}
