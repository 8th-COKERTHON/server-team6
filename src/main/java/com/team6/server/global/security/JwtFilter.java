package com.team6.server.global.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component public class JwtFilter extends OncePerRequestFilter {
 private final JwtProvider jwtProvider; public JwtFilter(JwtProvider p){this.jwtProvider=p;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");
  if(h!=null&&h.startsWith("Bearer ")){try{var c=jwtProvider.parse(h.substring(7));if("access".equals(c.get("type",String.class))){var a=new UsernamePasswordAuthenticationToken(c.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_"+c.get("role",String.class))));SecurityContextHolder.getContext().setAuthentication(a);}}catch(RuntimeException ignored){SecurityContextHolder.clearContext();}}
  chain.doFilter(req,res);
 }
}
