package com.team6.server.match.service;

import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.match.dto.MatchRequestDto;
import com.team6.server.match.dto.RingResponseDto;
import com.team6.server.match.entity.Match;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.match.repository.MatchingEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchingEventRepository matchingEventRepository;
    private final EpisodeRepository episodeRepository;
    private final MatchRepository matchRepository;
    private final CurrentMemberProvider currentMember;

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

        LocalDateTime startedAt = LocalDateTime.now();
        episodeA.markMatched(startedAt);
        episodeB.markMatched(startedAt);

        Match match = Match.builder()
                .memberId(memberId)
                .episodeAId(episodeA.getId())
                .episodeBId(episodeB.getId())
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
}
