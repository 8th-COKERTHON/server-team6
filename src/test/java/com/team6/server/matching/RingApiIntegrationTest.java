package com.team6.server.matching;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.EpisodeRanking;
import com.team6.server.episode.repository.EpisodeRankingRepository;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.matching.repository.*;
import com.team6.server.member.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
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
    @Autowired EpisodeRankingRepository rankings;
    @Autowired MatchingEventRepository events;
    @Autowired RingSessionRepository sessions;
    @Autowired EpisodeMatchRepository matches;
    @Autowired RankingScoreEventRepository scoreEvents;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private Member member;
    private String token;
    private MatchingEvent event;

    @BeforeEach
    void setUp() {
        clean();
        member = members.save(new Member("ring@example.com", passwordEncoder.encode("password123!"), "링 사용자"));
        token = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        var now = LocalDateTime.now();
        event = events.save(new MatchingEvent(MatchingEvent.Type.WEEKLY, "Monday Night Rivals",
                now.minusHours(1), now.plusHours(1), MatchingEvent.Status.OPEN, 10, 2));
        for (int i = 1; i <= 4; i++) {
            var episode = episodes.save(new Episode(member, "에피소드 " + i, "에피소드 내용 " + i, LocalDate.now()));
            rankings.save(new EpisodeRanking(episode));
        }
    }

    @AfterEach
    void tearDown() { clean(); }

    @Test
    void completesRoundsInOrderAndAwardsEachWinnerOnce() throws Exception {
        mockMvc.perform(get("/api/v1/ring/events").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].participationStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.items[0].roundCount").value(2));

        String started = mockMvc.perform(post("/api/v1/ring/sessions")
                        .header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("eventId", event.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentRound.roundNo").value(1))
                .andReturn().getResponse().getContentAsString();
        var data = objectMapper.readTree(started).path("data");
        long sessionId = data.path("sessionId").asLong();
        long firstWinner = data.path("currentRound").path("episodeA").path("episodeId").asLong();

        mockMvc.perform(post("/api/v1/ring/sessions")
                        .header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("eventId", event.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_409_2"));
        select(sessionId, 1, 999999L).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MATCH_400_1"));

        select(sessionId, 2, firstWinner).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_409_3"));

        String firstResult = select(sessionId, 1, firstWinner)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.winnerTitleScore").value(10))
                .andExpect(jsonPath("$.data.nextRound.roundNo").value(2))
                .andReturn().getResponse().getContentAsString();
        long secondWinner = objectMapper.readTree(firstResult).path("data").path("nextRound")
                .path("episodeA").path("episodeId").asLong();

        select(sessionId, 1, firstWinner).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_409_3"));
        select(sessionId, 2, secondWinner).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedRounds").value(2))
                .andExpect(jsonPath("$.data.nextRound").doesNotExist());

        mockMvc.perform(get("/api/v1/ring/sessions/{id}", sessionId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalScoreAwarded").value(20));
    }

    @Test
    void rejectsInsufficientEpisodesAndHidesAnotherMembersSession() throws Exception {
        mockMvc.perform(get("/api/v1/ring/events")).andExpect(status().isUnauthorized());
        rankings.deleteAll();
        episodes.deleteAll();
        mockMvc.perform(post("/api/v1/ring/sessions").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("eventId", event.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MATCH_400_3"));

        mockMvc.perform(get("/api/v1/ring/sessions/999999").header("Authorization", bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MATCH_404_3"));
    }

    private org.springframework.test.web.servlet.ResultActions select(long sessionId, int round, long winner) throws Exception {
        return mockMvc.perform(post("/api/v1/ring/sessions/{sessionId}/rounds/{round}/result", sessionId, round)
                .header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerEpisodeId", winner))));
    }

    private void clean() {
        scoreEvents.deleteAll();
        matches.deleteAll();
        sessions.deleteAll();
        rankings.deleteAll();
        episodes.deleteAll();
        events.deleteAll();
        members.deleteAll();
    }

    private String bearer() { return "Bearer " + token; }
}
