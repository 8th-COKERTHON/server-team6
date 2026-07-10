package com.team6.server.global.security;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.member.Member;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentMemberProvider {
    private final MemberRepository members;

    public CurrentMemberProvider(MemberRepository members) {
        this.members = members;
    }

    public Member require(Authentication authentication) {
        try {
            Long memberId = Long.valueOf(authentication.getName());
            return members.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
