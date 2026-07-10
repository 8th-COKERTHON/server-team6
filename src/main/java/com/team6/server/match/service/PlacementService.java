package com.team6.server.match.service;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.match.dto.ShowSessionResponse;
import com.team6.server.match.entity.Match;
import com.team6.server.match.entity.ShowSession;
import com.team6.server.match.entity.MatchingEvent;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.match.repository.ShowSessionRepository;
import com.team6.server.match.repository.MatchingEventRepository;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlacementService {
    public static final String ONBOARDING = "PLACEMENT_ONBOARDING";
    public static final String ADDITIONAL = "PLACEMENT_ADDITIONAL";
    private static final BigDecimal PLACEMENT_MULTIPLIER = new BigDecimal("2.00");

    private final CurrentMemberProvider currentMember;
    private final MemberRepository members;
    private final EpisodeRepository episodes;
    private final ShowSessionRepository sessions;
    private final MatchRepository matches;
    private final MatchingEventRepository events;
    private final RankingEpisodeScoreRepository rankings;
    private final Clock clock;

    public PlacementService(CurrentMemberProvider currentMember, MemberRepository members,
                            EpisodeRepository episodes, ShowSessionRepository sessions,
                            MatchRepository matches, MatchingEventRepository events,
                            RankingEpisodeScoreRepository rankings, Clock clock) {
        this.currentMember = currentMember;
        this.members = members;
        this.episodes = episodes;
        this.sessions = sessions;
        this.matches = matches;
        this.events = events;
        this.rankings = rankings;
        this.clock = clock;
    }

    @Transactional
    public ShowSessionResponse startOnboarding(Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        var member = members.findWithLockById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.isOnboardingCompleted()) {
            throw new BusinessException(ErrorCode.PLACEMENT_ALREADY_COMPLETED);
        }
        var inProgress = sessions.findFirstByMemberIdAndSessionTypeAndStatusOrderByStartedAtDesc(
                memberId, ONBOARDING, "IN_PROGRESS");
        if (inProgress.isPresent()) {
            return response(inProgress.get());
        }
        List<Episode> selected = episodes.findOnboardingCandidatesForUpdate(memberId, Episode.Status.AVAILABLE,
                Episode.PlacementStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 6));
        if (selected.size() != 5) throw new BusinessException(ErrorCode.INSUFFICIENT_PLACEMENT_EPISODES,
                "온보딩에는 배치 전 에피소드가 정확히 5개 필요합니다.");
        selected.forEach(Episode::startPlacement);
        LocalDateTime now = LocalDateTime.now(clock);
        MatchingEvent event = placementEvent("PO-" + memberId, "온보딩 배치전", ONBOARDING, 10, now);
        ShowSession session = sessions.save(ShowSession.builder()
                .eventId(event.getId()).memberId(memberId).sessionType(ONBOARDING).totalRounds(10)
                .scoreMultiplier(PLACEMENT_MULTIPLIER).startedAt(now).build());
        List<Match> generated = new ArrayList<>(10);
        int order = 1;
        for (int i = 0; i < selected.size(); i++) {
            for (int j = i + 1; j < selected.size(); j++) {
                generated.add(newMatch(session.getId(), memberId, selected.get(i).getId(),
                        selected.get(j).getId(), ONBOARDING, order++, now));
            }
        }
        matches.saveAll(generated);
        member.startPlacement();
        return response(session);
    }

    @Transactional
    public ShowSessionResponse startAdditional(Long episodeId, Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        members.findWithLockById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        List<Episode> targetResult = episodes.findAllByIdWithPessimisticLock(List.of(episodeId));
        if (targetResult.size() != 1) throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        Episode target = targetResult.getFirst();
        if (!target.getMember().getId().equals(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (target.getStatus() != Episode.Status.AVAILABLE) throw new BusinessException(ErrorCode.EPISODE_ALREADY_MATCHED);
        if (target.getPlacementStatus() != Episode.PlacementStatus.PENDING) {
            throw new BusinessException(ErrorCode.PLACEMENT_ALREADY_COMPLETED);
        }
        long targetScore = rankings.findById(target.getId()).orElseThrow(
                () -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND)).getTitleScore();
        List<Episode> opponents = episodes.findPlacementOpponentsForUpdate(memberId, target.getId(), targetScore,
                Episode.Status.AVAILABLE, Episode.PlacementStatus.COMPLETED,
                org.springframework.data.domain.PageRequest.of(0, 5));
        if (opponents.size() != 5) throw new BusinessException(ErrorCode.INSUFFICIENT_PLACEMENT_EPISODES,
                "배치전을 완료한 기존 에피소드 5개가 필요합니다.");
        target.startPlacement();
        LocalDateTime now = LocalDateTime.now(clock);
        MatchingEvent event = placementEvent("PA-" + memberId + "-" + episodeId,
                "추가 에피소드 배치전", ADDITIONAL, 5, now);
        ShowSession session = sessions.save(ShowSession.builder()
                .eventId(event.getId()).memberId(memberId).sessionType(ADDITIONAL)
                .primaryEpisodeId(target.getId()).totalRounds(5)
                .scoreMultiplier(PLACEMENT_MULTIPLIER).startedAt(now).build());
        List<Match> generated = new ArrayList<>(5);
        int order = 1;
        for (Episode opponent : opponents) {
            generated.add(newMatch(session.getId(), memberId, target.getId(), opponent.getId(),
                    ADDITIONAL, order++, now));
        }
        matches.saveAll(generated);
        return response(session);
    }

    static Match newMatch(Long sessionId, Long memberId, Long episodeAId, Long episodeBId,
                          String type, int order, LocalDateTime now) {
        return Match.builder().sessionId(sessionId).memberId(memberId)
                .episodeAId(episodeAId).episodeBId(episodeBId).matchType(type)
                .matchOrder(order).status("IN_PROGRESS").startedAt(now).build();
    }

    private MatchingEvent placementEvent(String periodKey, String title, String matchType,
                                          int rounds, LocalDateTime now) {
        return events.findByEventTypeAndPeriodKey("SPECIAL", periodKey).orElseGet(() ->
                events.save(MatchingEvent.builder().eventType("SPECIAL").periodKey(periodKey).title(title)
                        .description("회원별 Placement 내부 이벤트").matchType(matchType)
                        .startsAt(now.minusDays(1)).endsAt(now.plusYears(1)).status("OPEN")
                        .scoreReward(0L).roundCount(rounds).scoreMultiplier(PLACEMENT_MULTIPLIER).build()));
    }

    private ShowSessionResponse response(ShowSession session) {
        return new ShowSessionResponse(session.getId(), session.getSessionType(), session.getStatus(),
                session.getTotalRounds(), session.getCompletedRounds());
    }
}
