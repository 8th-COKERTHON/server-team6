package com.team6.server.member.service;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.CurrentMemberProvider;
import com.team6.server.member.Member;
import com.team6.server.member.dto.MemberMeResponse;
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
    private final Clock clock;

    public MemberService(CurrentMemberProvider currentMember, MemberRepository members, Clock clock) {
        this.currentMember = currentMember;
        this.members = members;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Authentication authentication) {
        return response(currentMember.require(authentication));
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
