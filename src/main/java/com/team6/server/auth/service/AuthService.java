package com.team6.server.auth.service;

import com.team6.server.auth.dto.LoginRequest;
import com.team6.server.auth.dto.LoginResponse;
import com.team6.server.auth.dto.RefreshRequest;
import com.team6.server.auth.dto.SignUpRequest;
import com.team6.server.auth.dto.TokenResponse;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.member.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final JwtProvider jwt;

    public AuthService(MemberRepository members, PasswordEncoder encoder, JwtProvider jwt) {
        this.members = members;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Long signUp(SignUpRequest request) {
        if (members.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }
        return members.save(new Member(request.email(), encoder.encode(request.password()), request.name())).getId();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var member = members.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!encoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        var tokens = tokens(member);
        return new LoginResponse(tokens.accessToken(), tokens.refreshToken(), tokens.tokenType(),
                tokens.expiresIn(), member.getName(), member.getEmail());
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        var claims = jwt.parseRefreshToken(request.refreshToken());
        Long memberId = Long.valueOf(claims.getSubject());
        return tokens(members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND)));
    }

    private TokenResponse tokens(Member member) {
        String role = member.getRole().name();
        return new TokenResponse(jwt.createAccessToken(member.getId(), role),
                jwt.createRefreshToken(member.getId(), role), "Bearer", jwt.accessExpiresInSeconds());
    }
}
