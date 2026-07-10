package com.team6.server.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.security.JwtProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MemberOnboardingApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private String bearer;
    private String email;

    @BeforeEach
    void setUp() {
        members.deleteAll();
        email = "onboarding@example.com";
        var member = members.save(new Member(email, passwordEncoder.encode("password123!"), "온보딩 사용자"));
        bearer = "Bearer " + jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    @Test
    void exposesIncompleteStateThenCompletesIdempotently() throws Exception {
        mockMvc.perform(get("/api/v1/members/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.data.onboardingCompletedAt").doesNotExist());

        String first = mockMvc.perform(post("/api/v1/members/me/onboarding/complete")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.onboardingCompletedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String completedAt = objectMapper.readTree(first).path("data").path("onboardingCompletedAt").asText();

        mockMvc.perform(post("/api/v1/members/me/onboarding/complete").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompletedAt").value(completedAt));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.onboardingCompletedAt").value(completedAt));
    }

    @Test
    void memberEndpointsRequireAuthenticationAndLoginStartsIncomplete() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_4"));
        mockMvc.perform(post("/api/v1/members/me/onboarding/complete"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.data.onboardingCompletedAt").doesNotExist());
    }
}
