package com.team6.server.auth;
import static com.team6.server.auth.AuthDtos.*;
import com.team6.server.global.exception.*;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.member.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional public class AuthService {
 private final MemberRepository members;private final PasswordEncoder encoder;private final JwtProvider jwt;
 public AuthService(MemberRepository m,PasswordEncoder e,JwtProvider j){members=m;encoder=e;jwt=j;}
 public Long signUp(SignUpRequest r){if(members.existsByEmail(r.email()))throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATED);return members.save(new Member(r.email(),encoder.encode(r.password()),r.name())).getId();}
 @Transactional(readOnly=true) public TokenResponse login(LoginRequest r){var m=members.findByEmail(r.email()).orElseThrow(()->new BusinessException(ErrorCode.LOGIN_FAILED));if(!encoder.matches(r.password(),m.getPassword()))throw new BusinessException(ErrorCode.LOGIN_FAILED);return tokens(m);}
 @Transactional(readOnly=true) public TokenResponse refresh(RefreshRequest r){var claims=jwt.parseRefreshToken(r.refreshToken());Long id=Long.valueOf(claims.getSubject());return tokens(members.findById(id).orElseThrow(()->new BusinessException(ErrorCode.MEMBER_NOT_FOUND)));}
 private TokenResponse tokens(Member m){String role=m.getRole().name();return new TokenResponse(jwt.createAccessToken(m.getId(),role),jwt.createRefreshToken(m.getId(),role),"Bearer",jwt.accessExpiresInSeconds());}
}
