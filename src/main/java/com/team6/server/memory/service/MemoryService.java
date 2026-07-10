package com.team6.server.memory.service;

import com.team6.server.global.config.TimeConfig;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.memory.Memory;
import com.team6.server.memory.MemoryRanking;
import com.team6.server.memory.dto.*;
import com.team6.server.memory.repository.MemoryRankingRepository;
import com.team6.server.memory.repository.MemoryRepository;
import com.team6.server.matching.MemoryMatch;
import com.team6.server.matching.repository.MemoryMatchRepository;
import com.team6.server.member.Member;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@Transactional
public class MemoryService {
    private final MemoryRepository memories;
    private final MemoryRankingRepository rankings;
    private final CurrentMemberProvider currentMember;
    private final TitleSuggestionProvider titleSuggestions;
    private final Clock clock;
    private final MemoryMatchRepository matches;

    public MemoryService(MemoryRepository memories, MemoryRankingRepository rankings,
                         CurrentMemberProvider currentMember, TitleSuggestionProvider titleSuggestions, Clock clock,
                         MemoryMatchRepository matches) {
        this.memories = memories;
        this.rankings = rankings;
        this.currentMember = currentMember;
        this.titleSuggestions = titleSuggestions;
        this.clock = clock;
        this.matches = matches;
    }

    public CreateMemoryResponse create(CreateMemoryRequest request, Authentication authentication) {
        var member = currentMember.require(authentication);
        if (request.memoryDate().isAfter(LocalDate.now(clock.withZone(TimeConfig.SERVICE_ZONE)))) {
            throw new BusinessException(ErrorCode.INVALID_MEMORY_DATE);
        }
        var memory = memories.save(new Memory(member, request.title().strip(), request.content().strip(), request.memoryDate()));
        rankings.save(new MemoryRanking(memory));
        memories.flush();
        long availableCount = memories.countByMemberIdAndStatus(member.getId(), Memory.Status.AVAILABLE);
        boolean hasActiveMatch = matches.existsByMemberIdAndStatus(member.getId(), MemoryMatch.Status.IN_PROGRESS);
        return new CreateMemoryResponse(memory.getId(), memory.getStatus().name(), 0, null,
                availableCount, availableCount >= 2 && !hasActiveMatch, memory.getCreatedAt());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TitleSuggestionResponse suggestTitle(TitleSuggestionRequest request, Authentication authentication) {
        currentMember.require(authentication);
        return new TitleSuggestionResponse(titleSuggestions.suggest(request.content()));
    }

    @Transactional(readOnly = true)
    public MemoryDetailResponse getDetail(Long memoryId, Authentication authentication) {
        var member = currentMember.require(authentication);
        var memory = memories.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMORY_NOT_FOUND));
        boolean admin = member.getRole() == Member.Role.ADMIN;
        if (!admin && !memory.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.MEMORY_NOT_FOUND);
        }
        var ranking = rankings.findById(memoryId).orElse(null);
        return new MemoryDetailResponse(
                memory.getId(), memory.getMember().getId(), memory.getTitle(), memory.getContent(),
                memory.getMemoryDate(), memory.getStatus().name(), memory.getMatchedAt(), ranking != null,
                ranking == null ? null : ranking.getTitleScore(),
                ranking == null ? null : ranking.getCurrentTitleId(),
                ranking == null ? null : ranking.getVersion(),
                memory.getCreatedAt(), memory.getUpdatedAt());
    }
}
