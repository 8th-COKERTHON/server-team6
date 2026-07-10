package com.team6.server.episode.service;

import com.team6.server.global.config.TimeConfig;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.episode.Episode;
import com.team6.server.episode.EpisodeRanking;
import com.team6.server.episode.dto.*;
import com.team6.server.episode.repository.EpisodeRankingRepository;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.member.Member;
import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@Transactional
public class EpisodeService {
    private final EpisodeRepository episodes;
    private final EpisodeRankingRepository rankings;
    private final CurrentMemberProvider currentMember;
    private final TitleSuggestionProvider titleSuggestions;
    private final Clock clock;
    private final EpisodeCursorCodec cursors;

    public EpisodeService(EpisodeRepository episodes, EpisodeRankingRepository rankings,
                         CurrentMemberProvider currentMember, TitleSuggestionProvider titleSuggestions, Clock clock,
                         EpisodeCursorCodec cursors) {
        this.episodes = episodes;
        this.rankings = rankings;
        this.currentMember = currentMember;
        this.titleSuggestions = titleSuggestions;
        this.clock = clock;
        this.cursors = cursors;
    }

    public CreateEpisodeResponse create(CreateEpisodeRequest request, Authentication authentication) {
        var member = currentMember.require(authentication);
        if (request.episodeDate().isAfter(LocalDate.now(clock.withZone(TimeConfig.SERVICE_ZONE)))) {
            throw new BusinessException(ErrorCode.INVALID_EPISODE_DATE);
        }
        var episode = episodes.save(new Episode(member, request.title().strip(), request.content().strip(), request.episodeDate()));
        rankings.save(new EpisodeRanking(episode));
        episodes.flush();
        long availableCount = episodes.countByMemberIdAndStatus(member.getId(), Episode.Status.AVAILABLE);
        return new CreateEpisodeResponse(episode.getId(), episode.getStatus().name(), 0, null,
                availableCount, false, episode.getCreatedAt());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TitleSuggestionResponse suggestTitle(TitleSuggestionRequest request, Authentication authentication) {
        currentMember.require(authentication);
        return new TitleSuggestionResponse(titleSuggestions.suggest(request.content()));
    }

    @Transactional(readOnly = true)
    public EpisodeDetailResponse getDetail(Long episodeId, Authentication authentication) {
        var member = currentMember.require(authentication);
        var episode = episodes.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
        boolean admin = member.getRole() == Member.Role.ADMIN;
        if (!admin && !episode.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
        }
        var ranking = rankings.findById(episodeId).orElse(null);
        return new EpisodeDetailResponse(
                episode.getId(), episode.getMember().getId(), episode.getTitle(), episode.getContent(),
                episode.getEpisodeDate(), episode.getStatus().name(), episode.getMatchedAt(), ranking != null,
                ranking == null ? null : ranking.getTitleScore(),
                ranking == null ? null : ranking.getCurrentTitleId(),
                ranking == null ? null : ranking.getVersion(),
                episode.getCreatedAt(), episode.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public EpisodeListResponse getAll(String statusValue, int size, String cursorValue,
                                     Authentication authentication) {
        var member = currentMember.require(authentication);
        Episode.Status status = parseStatus(statusValue);
        var cursor = cursors.decode(cursorValue);
        var page = episodes.findPage(member.getId(), status, cursor.createdAt(), cursor.episodeId(),
                PageRequest.of(0, size + 1));
        boolean hasNext = page.size() > size;
        var selected = hasNext ? page.subList(0, size) : page;
        var rankingMap = rankings.findAllById(selected.stream().map(Episode::getId).toList()).stream()
                .collect(Collectors.toMap(EpisodeRanking::getEpisodeId, Function.identity()));
        var items = selected.stream().map(episode -> {
            var ranking = rankingMap.get(episode.getId());
            return new EpisodeListItemResponse(episode.getId(), episode.getTitle(), preview(episode.getContent()),
                    episode.getEpisodeDate(), episode.getStatus().name(), ranking != null,
                    ranking == null ? null : ranking.getTitleScore(),
                    ranking == null ? null : ranking.getCurrentTitleId(),
                    episode.getMatchedAt(), episode.getCreatedAt());
        }).toList();
        String nextCursor = hasNext && !selected.isEmpty()
                ? cursors.encode(selected.getLast().getCreatedAt(), selected.getLast().getId()) : null;
        return new EpisodeListResponse(items, nextCursor, hasNext);
    }

    private Episode.Status parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Episode.Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "올바르지 않은 에피소드 상태입니다.");
        }
    }

    private String preview(String content) {
        String normalized = content.replaceAll("\\s+", " ").strip();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "…";
    }
}
