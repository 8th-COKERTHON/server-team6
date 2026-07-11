package com.team6.server.matching;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.member.Member;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RingApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository members;
    @Autowired EpisodeRepository episodes;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private Member member;
    private String token;

    @BeforeEach
    void setUp() {
        member = members.save(new Member(
                "ring-" + System.nanoTime() + "@example.com",
                passwordEncoder.encode("password123!"), "링 사용자"));
        token = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    @Test
    void ringRequiresAuthenticationAndListsAvailableEpisodes() throws Exception {
        episodes.save(new Episode(member, "첫 번째", "첫 번째 내용", LocalDate.now()));
        Episode matched = episodes.save(new Episode(member, "매칭됨", "매칭된 내용", LocalDate.now()));
        matched.markMatched(java.time.LocalDateTime.now());

        mockMvc.perform(get("/api/v1/ring"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/ring").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableEpisodes.length()").value(1))
                .andExpect(jsonPath("$.data.availableEpisodes[0].title").value("첫 번째"));
    }

    @Test
    void startsMatchRejectsReuseAndCancelRestoresEpisodes() throws Exception {
        Episode first = episodes.save(new Episode(member, "첫 번째", "첫 번째 내용", LocalDate.now()));
        Episode second = episodes.save(new Episode(member, "두 번째", "두 번째 내용", LocalDate.now()));

        String response = mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchRequest(first.getId(), second.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();
        long matchId = objectMapper.readTree(response).path("data").asLong();

        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchRequest(first.getId(), second.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EPISODE_409_2"));

        mockMvc.perform(delete("/api/v1/matches/{matchId}", matchId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/ring").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableEpisodes.length()").value(2));
    }

    @Test
    void rejectsSameEpisodeMissingInputAndAnotherMembersEpisode() throws Exception {
        Episode own = episodes.save(new Episode(member, "본인", "본인 내용", LocalDate.now()));
        Member another = members.save(new Member(
                "another-" + System.nanoTime() + "@example.com",
                passwordEncoder.encode("password123!"), "다른 사용자"));
        Episode others = episodes.save(new Episode(another, "타인", "타인 내용", LocalDate.now()));

        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchRequest(own.getId(), own.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MATCH_400_2"));

        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("episodeAId", own.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_400_2"));

        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchRequest(own.getId(), others.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GLOBAL_403_1"));
    }

    private String matchRequest(Long episodeAId, Long episodeBId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "episodeAId", episodeAId,
                "episodeBId", episodeBId));
    }

    private String bearer() {
        return "Bearer " + token;
    }
}
