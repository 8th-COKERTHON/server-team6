package com.team6.server.matching.service;

import com.team6.server.episode.Episode;
import com.team6.server.episode.EpisodeRanking;
import com.team6.server.episode.repository.EpisodeRankingRepository;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.matching.*;
import com.team6.server.matching.dto.*;
import com.team6.server.matching.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RingService {
    private final MatchingEventRepository events;
    private final RingSessionRepository sessions;
    private final EpisodeMatchRepository matches;
    private final EpisodeRepository episodes;
    private final EpisodeRankingRepository rankings;
    private final RankingScoreEventRepository scoreEvents;
    private final CurrentMemberProvider currentMember;
    private final Clock clock;
    private final MemberRepository members;

    public RingService(MatchingEventRepository events, RingSessionRepository sessions,
                       EpisodeMatchRepository matches, EpisodeRepository episodes,
                       EpisodeRankingRepository rankings, RankingScoreEventRepository scoreEvents,
                       CurrentMemberProvider currentMember, Clock clock, MemberRepository members) {
        this.events = events;
        this.sessions = sessions;
        this.matches = matches;
        this.episodes = episodes;
        this.rankings = rankings;
        this.scoreEvents = scoreEvents;
        this.currentMember = currentMember;
        this.clock = clock;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public RingEventListResponse getEvents(Authentication authentication) {
        var member = currentMember.require(authentication);
        var now = LocalDateTime.now(clock);
        var items = events.findByStatusAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByStartsAtDescIdDesc(
                MatchingEvent.Status.OPEN, now, now).stream().map(event -> {
            var session = sessions.findByEventIdAndMemberId(event.getId(), member.getId()).orElse(null);
            return new RingEventResponse(event.getId(), event.getEventType().name(), event.getTitle(),
                    event.getDescription(), event.getStartsAt(), event.getEndsAt(), event.getScoreReward(),
                    event.getRoundCount(), session == null ? "AVAILABLE" : session.getStatus().name(),
                    session == null ? null : session.getId());
        }).toList();
        return new RingEventListResponse(items);
    }

    public RingSessionResponse start(StartRingSessionRequest request, Authentication authentication) {
        var member = currentMember.require(authentication);
        members.findWithLockById(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (sessions.existsByMemberIdAndStatus(member.getId(), RingSession.Status.IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.RING_SESSION_ALREADY_STARTED);
        }
        var now = LocalDateTime.now(clock);
        var event = events.findByIdAndStatusAndStartsAtLessThanEqualAndEndsAtGreaterThan(
                        request.eventId(), MatchingEvent.Status.OPEN, now, now)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKLY_SHOW_NOT_AVAILABLE));
        if (sessions.findByEventIdAndMemberId(event.getId(), member.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.RING_SESSION_ALREADY_STARTED);
        }
        int episodeCount = event.getRoundCount() * 2;
        var selected = episodes.findAvailableForUpdate(member.getId(), Episode.Status.AVAILABLE,
                PageRequest.of(0, episodeCount));
        if (selected.size() < episodeCount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_RING_EPISODES);
        }
        try {
            var session = sessions.saveAndFlush(new RingSession(event.getId(), member.getId(),
                    event.getRoundCount(), now));
            for (int i = 0; i < event.getRoundCount(); i++) {
                matches.save(new EpisodeMatch(event.getId(), member.getId(), selected.get(i * 2).getId(),
                        selected.get(i * 2 + 1).getId(), session.getId(), i + 1, now));
            }
            matches.flush();
            return response(session, event, matches.findBySessionIdOrderByRoundNoAsc(session.getId()), selected);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.RING_SESSION_ALREADY_STARTED);
        }
    }

    @Transactional(readOnly = true)
    public RingSessionResponse getSession(Long sessionId, Authentication authentication) {
        var member = currentMember.require(authentication);
        var session = sessions.findByIdAndMemberId(sessionId, member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND));
        var event = events.findById(session.getEventId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKLY_SHOW_NOT_FOUND));
        var rounds = matches.findBySessionIdOrderByRoundNoAsc(sessionId);
        var episodeList = episodes.findAllById(rounds.stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getEpisodeAId(), match.getEpisodeBId()))
                .distinct().toList());
        return response(session, event, rounds, episodeList);
    }

    public RingRoundResultResponse selectWinner(Long sessionId, int roundNo, SelectRingWinnerRequest request,
                                                Authentication authentication) {
        var member = currentMember.require(authentication);
        var session = sessions.findWithLockByIdAndMemberId(sessionId, member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND));
        if (session.getStatus() != RingSession.Status.IN_PROGRESS || roundNo != session.getCompletedRounds() + 1) {
            throw new BusinessException(ErrorCode.RING_ROUND_OUT_OF_ORDER);
        }
        var match = matches.findBySessionIdAndRoundNo(sessionId, roundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        if (!request.winnerEpisodeId().equals(match.getEpisodeAId())
                && !request.winnerEpisodeId().equals(match.getEpisodeBId())) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_RESULT);
        }
        var locked = episodes.findAvailableForUpdate(member.getId(), Episode.Status.AVAILABLE,
                PageRequest.of(0, session.getTotalRounds() * 2));
        var byId = locked.stream().collect(Collectors.toMap(Episode::getId, Function.identity()));
        var episodeA = byId.get(match.getEpisodeAId());
        var episodeB = byId.get(match.getEpisodeBId());
        if (episodeA == null || episodeB == null) throw new BusinessException(ErrorCode.EPISODE_ALREADY_MATCHED);

        var event = events.findById(session.getEventId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKLY_SHOW_NOT_FOUND));
        var now = LocalDateTime.now(clock);
        match.complete(request.winnerEpisodeId(), now);
        episodeA.markMatched(now);
        episodeB.markMatched(now);
        var ranking = rankings.findById(request.winnerEpisodeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
        ranking.award(event.getScoreReward());
        if (event.getScoreReward() != 0) {
            scoreEvents.save(new RankingScoreEvent(match.getId(), request.winnerEpisodeId(),
                    event.getScoreReward(), now));
        }
        session.completeRound(now);

        RingRoundResponse next = null;
        if (session.getStatus() == RingSession.Status.IN_PROGRESS) {
            var nextMatch = matches.findBySessionIdAndRoundNo(sessionId, roundNo + 1)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            var nextEpisodes = episodes.findAllById(java.util.List.of(nextMatch.getEpisodeAId(), nextMatch.getEpisodeBId()));
            var nextMap = nextEpisodes.stream().collect(Collectors.toMap(Episode::getId, Function.identity()));
            next = round(nextMatch, session.getTotalRounds(), nextMap);
        }
        return new RingRoundResultResponse(sessionId, session.getStatus().name(), session.getCompletedRounds(),
                session.getTotalRounds(), request.winnerEpisodeId(), event.getScoreReward(),
                ranking.getTitleScore(), next);
    }

    private RingSessionResponse response(RingSession session, MatchingEvent event,
                                         java.util.List<EpisodeMatch> rounds, java.util.List<Episode> episodeList) {
        Map<Long, Episode> episodeMap = episodeList.stream().collect(Collectors.toMap(Episode::getId, Function.identity()));
        RingRoundResponse current = rounds.stream()
                .filter(match -> match.getStatus() == EpisodeMatch.Status.IN_PROGRESS)
                .findFirst().map(match -> round(match, session.getTotalRounds(), episodeMap)).orElse(null);
        return new RingSessionResponse(session.getId(), event.getId(), event.getTitle(), session.getStatus().name(),
                session.getTotalRounds(), session.getCompletedRounds(), current,
                (long) session.getCompletedRounds() * event.getScoreReward(), session.getStartedAt(), session.getCompletedAt());
    }

    private RingRoundResponse round(EpisodeMatch match, int totalRounds, Map<Long, Episode> episodeMap) {
        return new RingRoundResponse(match.getId(), match.getRoundNo(), totalRounds, match.getStatus().name(),
                episode(episodeMap.get(match.getEpisodeAId())), episode(episodeMap.get(match.getEpisodeBId())),
                match.getWinnerEpisodeId(), match.getStartedAt(), match.getCompletedAt());
    }

    private RingEpisodeResponse episode(Episode episode) {
        if (episode == null) throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        return new RingEpisodeResponse(episode.getId(), episode.getTitle(), episode.getContent(), episode.getEpisodeDate());
    }
}
