package com.team6.server.match.service;

import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.match.dto.MatchRequestDto;
import com.team6.server.match.dto.MatchResultResponseDto;
import com.team6.server.match.dto.RingResponseDto;
import com.team6.server.match.entity.Match;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.match.repository.MatchingEventRepository;
import com.team6.server.match.repository.ShowSessionRepository;
import com.team6.server.auth.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.team6.server.ranking.entity.RankingEpisodeScore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import com.team6.server.ranking.repository.RankingScoreEventRepository;
import com.team6.server.ranking.repository.TitleRepository;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchingEventRepository matchingEventRepository;
    private final EpisodeRepository episodeRepository;
    private final MatchRepository matchRepository;
    private final CurrentMemberProvider currentMember;
    private final ShowSessionRepository showSessionRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    private final RankingEpisodeScoreRepository episodeRankingRepository;
    private final RankingScoreEventRepository rankingScoreEventRepository;
    private final TitleRepository titleRepository;

    @Transactional(readOnly = true)
    public RingResponseDto getRingScreen(Authentication authentication) {
        Long memberId = currentMember.require(authentication).getId();
        var activeEvents = matchingEventRepository.findByStatus("OPEN").stream()
                .map(RingResponseDto.ActiveEventDto::from)
                .toList();

        var availableEpisodes = episodeRepository
                .findAllByMemberIdAndStatusOrderByCreatedAtDescIdDesc(memberId, Episode.Status.AVAILABLE)
                .stream()
                .map(episode -> new RingResponseDto.AvailableEpisodeDto(
                        episode.getId(), episode.getTitle(), episode.getEpisodeDate().toString()))
                .toList();

        return new RingResponseDto(null, availableEpisodes, null, activeEvents);
    }

    @Transactional
    public Long startMatch(Authentication authentication, MatchRequestDto request) {
        Long memberId = currentMember.require(authentication).getId();
        if (request.episodeAId().equals(request.episodeBId())) {
            throw new BusinessException(ErrorCode.SAME_EPISODE_MATCH);
        }

        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(request.episodeAId(), request.episodeBId()));
        if (episodes.size() != 2) {
            throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        }

        Episode episodeA = find(episodes, request.episodeAId());
        Episode episodeB = find(episodes, request.episodeBId());
        if (!episodeA.getMember().getId().equals(memberId)
                || !episodeB.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (episodeA.getStatus() != Episode.Status.AVAILABLE
                || episodeB.getStatus() != Episode.Status.AVAILABLE) {
            throw new BusinessException(ErrorCode.EPISODE_ALREADY_MATCHED);
        }
        if (episodeA.getPlacementStatus() == Episode.PlacementStatus.IN_PROGRESS
                || episodeB.getPlacementStatus() == Episode.PlacementStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.EPISODE_ALREADY_MATCHED);
        }

        LocalDateTime startedAt = LocalDateTime.now();
        episodeA.markMatched(startedAt);
        episodeB.markMatched(startedAt);

        Match match = Match.builder()
                .memberId(memberId)
                .episodeAId(episodeA.getId())
                .episodeBId(episodeB.getId())
                .matchType("GENERAL")
                .status("IN_PROGRESS")
                .startedAt(startedAt)
                .build();
        return matchRepository.save(match).getId();
    }

    @Transactional
    public void cancelMatch(Authentication authentication, Long matchId) {
        Long memberId = currentMember.require(authentication).getId();
        Match match = matchRepository.findByIdWithPessimisticLock(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        if (!match.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MATCH_NOT_FOUND);
        }
        if (!"IN_PROGRESS".equals(match.getStatus())) {
            throw new BusinessException(ErrorCode.MATCH_ALREADY_COMPLETED);
        }
        if (match.getSessionId() != null) {
            throw new BusinessException(ErrorCode.RING_ROUND_OUT_OF_ORDER, "Show 경기는 취소할 수 없습니다.");
        }

        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(match.getEpisodeAId(), match.getEpisodeBId()));
        if (episodes.size() != 2) {
            throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        }
        episodes.forEach(Episode::restoreAvailable);
        matchRepository.delete(match);
    }

    private Episode find(List<Episode> episodes, Long id) {
        return episodes.stream().filter(episode -> episode.getId().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
    }

    /* 대결 결과 확정 로직 */
    @Transactional
    public MatchResultResponseDto completeMatch(
            Authentication authentication,
            Long matchId,
            com.team6.server.match.dto.MatchResultRequestDto request
    ) {
        Long memberId = currentMember.require(authentication).getId();
        Long winnerId = request.getWinnerEpisodeId();
        if (winnerId == null) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_RESULT);
        }

        // 대결 검증
        Match match = matchRepository.findByIdWithPessimisticLock(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        if (!"IN_PROGRESS".equals(match.getStatus())) {
            throw new BusinessException(ErrorCode.MATCH_ALREADY_COMPLETED);
        }

        // 소유권 검증
        if (!match.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MATCH_NOT_FOUND);
        }

        // 승자 검증
        if (!winnerId.equals(match.getEpisodeAId()) && !winnerId.equals(match.getEpisodeBId())) {
            throw new BusinessException(ErrorCode.INVALID_MATCH_RESULT);
        }
        if (match.getSessionId() != null) {
            Long nextMatchId = matchRepository.findFirstBySessionIdAndStatusOrderByMatchOrderAsc(
                            match.getSessionId(), "IN_PROGRESS")
                    .map(Match::getId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND));
            if (!nextMatchId.equals(matchId)) {
                throw new BusinessException(ErrorCode.RING_ROUND_OUT_OF_ORDER);
            }
        }

        // 점수 원장 기록 - 중복 지급 방지용 멱등 키 적용
        String winnerEventKey = "MATCH_" + matchId + "_WINNER";
        String loserEventKey = "MATCH_" + matchId + "_LOSER";
        if (rankingScoreEventRepository.existsByEventKey(winnerEventKey)
                || rankingScoreEventRepository.existsByEventKey(loserEventKey)) {
            throw new BusinessException(ErrorCode.MATCH_ALREADY_COMPLETED);
        }

        Long loserId = winnerId.equals(match.getEpisodeAId()) ? match.getEpisodeBId() : match.getEpisodeAId();
        RankingEpisodeScore winnerRanking = findOrCreateRanking(winnerId);
        RankingEpisodeScore loserRanking = findOrCreateRanking(loserId);
        long winnerScoreBefore = winnerRanking.getTitleScore();
        long loserScoreBefore = loserRanking.getTitleScore();
        ScoreDelta baseDelta = calculateScoreDelta(winnerScoreBefore, loserScoreBefore);
        BigDecimal multiplier = match.getSessionId() == null ? BigDecimal.ONE
                : showSessionRepository.findByIdWithPessimisticLock(match.getSessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND)).getScoreMultiplier();
        ScoreDelta scoreDelta = new ScoreDelta(
                multiplier.multiply(BigDecimal.valueOf(baseDelta.winnerDelta())).longValueExact(),
                multiplier.multiply(BigDecimal.valueOf(baseDelta.loserDelta())).longValueExact());

        winnerRanking.applyDelta(scoreDelta.winnerDelta());
        loserRanking.applyDelta(scoreDelta.loserDelta());

        updateTitle(winnerRanking);
        updateTitle(loserRanking);
        episodeRankingRepository.save(winnerRanking);
        episodeRankingRepository.save(loserRanking);

        LocalDateTime completedAt = LocalDateTime.now(clock);
        com.team6.server.ranking.entity.RankingScoreEvent winnerScoreEvent = com.team6.server.ranking.entity.RankingScoreEvent.builder()
                .eventKey(winnerEventKey)
                .episodeId(winnerId)
                .scoreType("TITLE")
                .delta(scoreDelta.winnerDelta())
                .sourceType("MATCH_COMPLETED")
                .sourceId(matchId)
                .occurredAt(completedAt)
                .build();
        rankingScoreEventRepository.save(winnerScoreEvent);

        com.team6.server.ranking.entity.RankingScoreEvent loserScoreEvent = com.team6.server.ranking.entity.RankingScoreEvent.builder()
                .eventKey(loserEventKey)
                .episodeId(loserId)
                .scoreType("TITLE")
                .delta(scoreDelta.loserDelta())
                .sourceType("MATCH_COMPLETED")
                .sourceId(matchId)
                .occurredAt(completedAt)
                .build();
        rankingScoreEventRepository.save(loserScoreEvent);

        boolean winnerIsEpisodeA = winnerId.equals(match.getEpisodeAId());
        long episodeAScoreBefore = winnerIsEpisodeA ? winnerScoreBefore : loserScoreBefore;
        long episodeBScoreBefore = winnerIsEpisodeA ? loserScoreBefore : winnerScoreBefore;
        long episodeAScoreAfter = winnerIsEpisodeA ? winnerRanking.getTitleScore() : loserRanking.getTitleScore();
        long episodeBScoreAfter = winnerIsEpisodeA ? loserRanking.getTitleScore() : winnerRanking.getTitleScore();
        match.complete(winnerId, loserId, episodeAScoreBefore, episodeBScoreBefore,
                episodeAScoreAfter, episodeBScoreAfter, scoreDelta.winnerDelta(), scoreDelta.loserDelta(), completedAt);
        if (match.getSessionId() == null) {
            restoreMatchedEpisodes(match);
        } else {
            var session = showSessionRepository.findByIdWithPessimisticLock(match.getSessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RING_SESSION_NOT_FOUND));
            boolean sessionCompleted = session.completeRound(completedAt);
            if (sessionCompleted) {
                if (PlacementService.ONBOARDING.equals(session.getSessionType())) {
                    matchRepository.findBySessionIdOrderByMatchOrderAsc(session.getId()).stream()
                            .flatMap(item -> java.util.stream.Stream.of(item.getEpisodeAId(), item.getEpisodeBId()))
                            .distinct().sorted().map(episodeRepository::findById)
                            .map(optional -> optional.orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
                            .forEach(Episode::completePlacement);
                    memberRepository.findWithLockById(memberId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                            .completeOnboarding(completedAt);
                } else if (PlacementService.ADDITIONAL.equals(session.getSessionType())) {
                    episodeRepository.findById(session.getPrimaryEpisodeId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND))
                            .completePlacement();
                }
            }
        }

        return com.team6.server.match.dto.MatchResultResponseDto.builder()
                .matchId(match.getId())
                .status(match.getStatus())
                .winnerEpisodeId(winnerId)
                .winnerEpisodeScoreAwarded(scoreDelta.winnerDelta())
                .winnerEpisodeTitleScore(winnerRanking.getTitleScore())
                .winnerEpisodeTitle(winnerRanking.getCurrentTitle() != null ? winnerRanking.getCurrentTitle().getName() : null)
                .completedAt(match.getCompletedAt())
                .build();
    }

    private RankingEpisodeScore findOrCreateRanking(Long episodeId) {
        return episodeRankingRepository.findById(episodeId)
                .orElseGet(() -> RankingEpisodeScore.initial(episodeId));
    }

    private void updateTitle(RankingEpisodeScore ranking) {
        List<com.team6.server.ranking.entity.Title> titles = titleRepository.findAllByOrderByMinScoreAsc();
        com.team6.server.ranking.entity.Title matchedTitle = null;
        for (com.team6.server.ranking.entity.Title title : titles) {
            if (ranking.getTitleScore() >= title.getMinScore()) {
                matchedTitle = title;
            }
        }
        ranking.updateTitle(matchedTitle);
    }

    private void restoreMatchedEpisodes(Match match) {
        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(match.getEpisodeAId(), match.getEpisodeBId()));
        if (episodes.size() != 2) {
            throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        }
        episodes.forEach(Episode::restoreAvailable);
    }

    private ScoreDelta calculateScoreDelta(long winnerScore, long loserScore) {
        long diff = Math.abs(winnerScore - loserScore);
        if (diff <= 100) {
            return new ScoreDelta(50L, -50L);
        }

        boolean winnerWasStronger = winnerScore > loserScore;
        if (diff <= 250) {
            return winnerWasStronger ? new ScoreDelta(30L, -80L) : new ScoreDelta(80L, -30L);
        }
        if (diff <= 500) {
            return winnerWasStronger ? new ScoreDelta(20L, -120L) : new ScoreDelta(120L, -20L);
        }
        return winnerWasStronger ? new ScoreDelta(10L, -150L) : new ScoreDelta(150L, -10L);
    }

    private record ScoreDelta(long winnerDelta, long loserDelta) {
    }
}
