package com.team6.server.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.member.Member;
import com.team6.server.episode.repository.EpisodeRankingRepository;
import com.team6.server.episode.repository.EpisodeRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
class HomeEpisodeApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository members;
    @Autowired EpisodeRepository episodes;
    @Autowired EpisodeRankingRepository rankings;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        rankings.deleteAll();
        episodes.deleteAll();
        members.deleteAll();
        Member member = members.save(new Member("home@example.com", passwordEncoder.encode("password123!"), "홈 사용자"));
        accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    @Test
    void homeRequiresAuthenticationAndReturnsEmptyState() throws Exception {
        mockMvc.perform(get("/api/v1/home")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/home").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableEpisodeCount").value(0))
                .andExpect(jsonPath("$.data.canStartMatch").value(false))
                .andExpect(jsonPath("$.data.todayEpisode").doesNotExist())
                .andExpect(jsonPath("$.data.upcomingEvents").isEmpty());
    }

    @Test
    void multipleEpisodesCanBeRegisteredInOneDayWithoutEnablingRolledBackMatch() throws Exception {
        createEpisode("첫 번째 제목", LocalDate.now());

        mockMvc.perform(post("/api/v1/episodes")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(episodeBody("수정 가능한 두 번째 제목", LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.availableEpisodeCount").value(2))
                .andExpect(jsonPath("$.data.canStartMatch").value(false));

        mockMvc.perform(get("/api/v1/home").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayEpisode.title").value("수정 가능한 두 번째 제목"));
    }

    @Test
    void futureEpisodeDateIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/episodes")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(episodeBody("미래 에피소드", LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EPISODE_400_1"));
    }

    @Test
    void titleSuggestionIsOptionalAndAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/episodes/title-suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "면접에서 아쉽게 탈락했다."))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/episodes/title-suggestions")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "면접에서 아쉽게 탈락했다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("면접에서 아쉽게 탈락했다"));
    }

    @Test
    void episodeDetailShowsPersistedEpisodeAndRankingState() throws Exception {
        createEpisode("저장 확인 제목", LocalDate.now());
        Long episodeId = episodes.findAll().getFirst().getId();

        mockMvc.perform(get("/api/v1/episodes/{episodeId}", episodeId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.episodeId").value(episodeId))
                .andExpect(jsonPath("$.data.title").value("저장 확인 제목"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.rankingPresent").value(true))
                .andExpect(jsonPath("$.data.titleScore").value(0))
                .andExpect(jsonPath("$.data.rankingVersion").value(0));
    }

    @Test
    void episodeDetailAllowsAdminButHidesItFromAnotherUser() throws Exception {
        createEpisode("권한 확인 제목", LocalDate.now());
        Long episodeId = episodes.findAll().getFirst().getId();
        Member other = members.save(new Member("other@example.com", passwordEncoder.encode("password123!"), "다른 사용자"));
        Member admin = members.save(new Member("admin@example.com", passwordEncoder.encode("password123!"),
                "운영 사용자", Member.Role.ADMIN));

        mockMvc.perform(get("/api/v1/episodes/{episodeId}", episodeId)
                        .header("Authorization", "Bearer " + jwtProvider.createAccessToken(other.getId(), other.getRole().name())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EPISODE_404_1"));

        mockMvc.perform(get("/api/v1/episodes/{episodeId}", episodeId)
                        .header("Authorization", "Bearer " + jwtProvider.createAccessToken(admin.getId(), admin.getRole().name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.episodeId").value(episodeId));
    }

    @Test
    void episodeListIsOwnedNewestFirstAndCursorPaginated() throws Exception {
        createEpisode("첫 번째", LocalDate.now());
        createEpisode("두 번째", LocalDate.now());
        createEpisode("세 번째", LocalDate.now());
        Member other = members.save(new Member("list-other@example.com", passwordEncoder.encode("password123!"), "목록 외 사용자"));
        mockMvc.perform(post("/api/v1/episodes")
                        .header("Authorization", "Bearer " + jwtProvider.createAccessToken(other.getId(), other.getRole().name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(episodeBody("다른 사용자 에피소드", LocalDate.now())))
                .andExpect(status().isCreated());

        String firstPage = mockMvc.perform(get("/api/v1/episodes")
                        .header("Authorization", bearer()).param("status", "AVAILABLE").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].title").value("세 번째"))
                .andExpect(jsonPath("$.data.items[1].title").value("두 번째"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andReturn().getResponse().getContentAsString();
        String cursor = objectMapper.readTree(firstPage).path("data").path("nextCursor").asText();

        mockMvc.perform(get("/api/v1/episodes")
                        .header("Authorization", bearer()).param("size", "2").param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("첫 번째"))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void episodeListRejectsInvalidCursorAndSize() throws Exception {
        mockMvc.perform(get("/api/v1/episodes")
                        .header("Authorization", bearer()).param("cursor", "not-a-cursor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_400_1"));

        mockMvc.perform(get("/api/v1/episodes")
                        .header("Authorization", bearer()).param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_400_1"));

        mockMvc.perform(get("/api/v1/episodes")
                        .header("Authorization", bearer()).param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    private void createEpisode(String title, LocalDate date) throws Exception {
        mockMvc.perform(post("/api/v1/episodes")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(episodeBody(title, date)))
                .andExpect(status().isCreated());
    }

    private String episodeBody(String title, LocalDate date) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "content", "사용자가 직접 입력한 에피소드 내용입니다.",
                "episodeDate", date.toString()));
    }

    private String bearer() { return "Bearer " + accessToken; }
}
