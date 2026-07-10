package com.team6.server.global.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.team6.server.global.exception.BusinessException;
import com.team6.server.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component public class JwtFilter extends OncePerRequestFilter {
 public static final String AUTH_ERROR_ATTRIBUTE=JwtFilter.class.getName()+".AUTH_ERROR";
 private final JwtProvider jwtProvider; public JwtFilter(JwtProvider p){this.jwtProvider=p;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");
  if(h!=null){if(!h.startsWith("Bearer ")||h.length()==7){req.setAttribute(AUTH_ERROR_ATTRIBUTE,ErrorCode.INVALID_TOKEN);}else{try{var c=jwtProvider.parseAccessToken(h.substring(7));var a=new UsernamePasswordAuthenticationToken(c.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_"+c.get("role",String.class))));SecurityContextHolder.getContext().setAuthentication(a);}catch(BusinessException e){SecurityContextHolder.clearContext();req.setAttribute(AUTH_ERROR_ATTRIBUTE,e.getErrorCode());}}}
  chain.doFilter(req,res);
 }
}
