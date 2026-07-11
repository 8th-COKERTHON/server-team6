package com.team6.server.member.service;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.member.Member;
import com.team6.server.member.dto.MemberMeResponse;
import com.team6.server.member.dto.OnboardingStatusResponse;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.match.repository.ShowSessionRepository;
import com.team6.server.match.service.PlacementService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {
    private final CurrentMemberProvider currentMember;
    private final MemberRepository members;
    private final EpisodeRepository episodes;
    private final ShowSessionRepository sessions;
    private final Clock clock;

    public MemberService(CurrentMemberProvider currentMember, MemberRepository members, EpisodeRepository episodes,
                         ShowSessionRepository sessions, Clock clock) {
        this.currentMember = currentMember;
        this.members = members;
        this.episodes = episodes;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Authentication authentication) {
        return response(currentMember.require(authentication));
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(Authentication authentication) {
        var member = currentMember.require(authentication);
        var placement = sessions.findFirstByMemberIdAndSessionTypeOrderByStartedAtDesc(
                member.getId(), PlacementService.ONBOARDING).orElse(null);
        return new OnboardingStatusResponse(member.getOnboardingStatus().name(),
                placement != null && "IN_PROGRESS".equals(placement.getStatus()) ? placement.getId() : null,
                episodes.countByMemberId(member.getId()),
                placement == null ? 0 : placement.getCompletedRounds(),
                placement == null ? 0 : placement.getTotalRounds());
    }

    public MemberMeResponse completeOnboarding(Authentication authentication) {
        var authenticated = currentMember.require(authentication);
        var member = members.findWithLockById(authenticated.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.completeOnboarding(LocalDateTime.now(clock).truncatedTo(ChronoUnit.MICROS));
        return response(member);
    }

    private MemberMeResponse response(Member member) {
        return new MemberMeResponse(member.getId(), member.getName(), member.getEmail(),
                member.isOnboardingCompleted(), member.getOnboardingCompletedAt());
    }
}
