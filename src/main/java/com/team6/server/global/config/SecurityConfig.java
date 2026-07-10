package com.team6.server.global.config;
import com.team6.server.global.security.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtFilter filter,RestAuthenticationEntryPoint entry,RestAccessDeniedHandler denied)throws Exception{return http.csrf(x->x.disable()).cors(x->{}).sessionManagement(x->x.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(x->x.authenticationEntryPoint(entry).accessDeniedHandler(denied)).authorizeHttpRequests(x->x.requestMatchers("/api/v1/auth/**","/api/v1/sample/public","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/actuator/health").permitAll().requestMatchers(HttpMethod.OPTIONS,"/**").permitAll().requestMatchers("/api/**").authenticated().anyRequest().permitAll()).addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();}
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}") List<String> origins){var c=new CorsConfiguration();c.setAllowedOrigins(origins);c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
}
