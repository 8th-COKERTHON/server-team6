package com.team6.server.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.matching.repository.MatchingEventRepository;
import com.team6.server.matching.MatchingEvent;
import com.team6.server.matching.repository.MemoryMatchRepository;
import com.team6.server.member.Member;
import com.team6.server.memory.repository.MemoryRankingRepository;
import com.team6.server.memory.repository.MemoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class HomeMemoryApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository members;
    @Autowired MemoryRepository memories;
    @Autowired MemoryRankingRepository rankings;
    @Autowired MemoryMatchRepository matches;
    @Autowired MatchingEventRepository events;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        matches.deleteAll();
        rankings.deleteAll();
        memories.deleteAll();
        events.deleteAll();
        members.deleteAll();
        Member member = members.save(new Member("home@example.com", passwordEncoder.encode("password123!"), "홈 사용자"));
        accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    @Test
    void homeRequiresAuthenticationAndReturnsEmptyState() throws Exception {
        mockMvc.perform(get("/api/v1/home")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/home").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableMemoryCount").value(0))
                .andExpect(jsonPath("$.data.canStartMatch").value(false))
                .andExpect(jsonPath("$.data.todayMemory").doesNotExist())
                .andExpect(jsonPath("$.data.upcomingEvents").isEmpty());
    }

    @Test
    void multipleMemoriesCanBeRegisteredInOneDayAndEnableMatch() throws Exception {
        createMemory("첫 번째 제목", LocalDate.now());

        mockMvc.perform(post("/api/v1/memories")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memoryBody("수정 가능한 두 번째 제목", LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.availableMemoryCount").value(2))
                .andExpect(jsonPath("$.data.canStartMatch").value(true));

        mockMvc.perform(get("/api/v1/home").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayMemory.title").value("수정 가능한 두 번째 제목"));
    }

    @Test
    void futureMemoryDateIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/memories")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memoryBody("미래 기억", LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMORY_400_1"));
    }

    @Test
    void titleSuggestionIsOptionalAndAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/memories/title-suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "면접에서 아쉽게 탈락했다."))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/memories/title-suggestions")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "면접에서 아쉽게 탈락했다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("면접에서 아쉽게 탈락했다"));
    }

    @Test
    void homeReturnsOnlyScheduledFutureEvents() throws Exception {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        events.save(new MatchingEvent(MatchingEvent.Type.WEEKLY, "예정 회차", tomorrow,
                tomorrow.plusDays(1), MatchingEvent.Status.SCHEDULED, 10));
        events.save(new MatchingEvent(MatchingEvent.Type.MONTHLY, "초안 회차", tomorrow.plusHours(1),
                tomorrow.plusDays(2), MatchingEvent.Status.DRAFT, 20));

        mockMvc.perform(get("/api/v1/home").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.upcomingEvents.length()").value(1))
                .andExpect(jsonPath("$.data.upcomingEvents[0].title").value("예정 회차"))
                .andExpect(jsonPath("$.data.upcomingEvents[0].daysRemaining").value(1));
    }

    @Test
    void memoryDetailShowsPersistedMemoryAndRankingState() throws Exception {
        createMemory("저장 확인 제목", LocalDate.now());
        Long memoryId = memories.findAll().getFirst().getId();

        mockMvc.perform(get("/api/v1/memories/{memoryId}", memoryId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memoryId").value(memoryId))
                .andExpect(jsonPath("$.data.title").value("저장 확인 제목"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.rankingPresent").value(true))
                .andExpect(jsonPath("$.data.titleScore").value(0))
                .andExpect(jsonPath("$.data.rankingVersion").value(0));
    }

    @Test
    void memoryDetailAllowsAdminButHidesItFromAnotherUser() throws Exception {
        createMemory("권한 확인 제목", LocalDate.now());
        Long memoryId = memories.findAll().getFirst().getId();
        Member other = members.save(new Member("other@example.com", passwordEncoder.encode("password123!"), "다른 사용자"));
        Member admin = members.save(new Member("admin@example.com", passwordEncoder.encode("password123!"),
                "운영 사용자", Member.Role.ADMIN));

        mockMvc.perform(get("/api/v1/memories/{memoryId}", memoryId)
                        .header("Authorization", "Bearer " + jwtProvider.createAccessToken(other.getId(), other.getRole().name())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMORY_404_1"));

        mockMvc.perform(get("/api/v1/memories/{memoryId}", memoryId)
                        .header("Authorization", "Bearer " + jwtProvider.createAccessToken(admin.getId(), admin.getRole().name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memoryId").value(memoryId));
    }

    private void createMemory(String title, LocalDate date) throws Exception {
        mockMvc.perform(post("/api/v1/memories")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memoryBody(title, date)))
                .andExpect(status().isCreated());
    }

    private String memoryBody(String title, LocalDate date) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "content", "사용자가 직접 입력한 기억 내용입니다.",
                "memoryDate", date.toString()));
    }

    private String bearer() { return "Bearer " + accessToken; }
}
