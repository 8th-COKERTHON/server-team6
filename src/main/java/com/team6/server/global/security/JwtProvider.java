package com.team6.server.global.security;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class JwtProvider {
 private final SecretKey key; private final long accessMs; private final long refreshMs;
 public JwtProvider(@Value("${jwt.secret}") String secret,@Value("${jwt.access-expiration:1800000}") long accessMs,@Value("${jwt.refresh-expiration:1209600000}") long refreshMs){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.accessMs=accessMs;this.refreshMs=refreshMs;}
 public String createAccessToken(Long id,String role){return create(id,role,accessMs,"access");}
 public String createRefreshToken(Long id,String role){return create(id,role,refreshMs,"refresh");}
 private String create(Long id,String role,long ttl,String type){var now=new Date();return Jwts.builder().subject(id.toString()).claim("role",role).claim("type",type).issuedAt(now).expiration(new Date(now.getTime()+ttl)).signWith(key).compact();}
 public Claims parseAccessToken(String token){return parse(token,"access",ErrorCode.EXPIRED_ACCESS_TOKEN);}
 public Claims parseRefreshToken(String token){return parse(token,"refresh",ErrorCode.EXPIRED_REFRESH_TOKEN);}
 private Claims parse(String token,String expectedType,ErrorCode expiredCode){try{var claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();if(!expectedType.equals(claims.get("type",String.class)))throw new BusinessException(ErrorCode.INVALID_TOKEN);return claims;}catch(ExpiredJwtException e){throw new BusinessException(expiredCode);}catch(JwtException|IllegalArgumentException e){throw new BusinessException(ErrorCode.INVALID_TOKEN);}}
 public long accessExpiresInSeconds(){return Duration.ofMillis(accessMs).toSeconds();}
}
