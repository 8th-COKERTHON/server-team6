package com.team6.server.history;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.global.security.JwtProvider;
import com.team6.server.match.entity.Match;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.member.Member;
import com.team6.server.ranking.entity.RankingEpisodeScore;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HistoryApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository members;
    @Autowired EpisodeRepository episodes;
    @Autowired MatchRepository matches;
    @Autowired RankingEpisodeScoreRepository rankings;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtProvider jwtProvider;

    private Member member;
    private String token;

    @BeforeEach
    void setUp() {
        matches.deleteAll();
        rankings.deleteAll();
        episodes.deleteAll();
        members.deleteAll();

        member = members.save(new Member(
                "history@example.com",
                passwordEncoder.encode("password123!"),
                "기록 사용자"));
        token = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    }

    @Test
    void historyHomeRequiresAuthenticationAndReturnsChampionAndMatchRecords() throws Exception {
        Episode winner = episodes.save(new Episode(member, "취업 최종 탈락", "면접에서 아쉽게 떨어졌다.", LocalDate.of(2026, 7, 10)));
        Episode loser = episodes.save(new Episode(member, "팀플 갈등", "프로젝트 일정이 꼬였다.", LocalDate.of(2026, 7, 9)));
        rankings.save(RankingEpisodeScore.builder().episodeId(winner.getId()).titleScore(1300L).build());
        rankings.save(RankingEpisodeScore.builder().episodeId(loser.getId()).titleScore(900L).build());
        matches.save(completedMatch(winner, loser, winner));

        mockMvc.perform(get("/api/v1/history"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/history").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.championRecords.length()").value(2))
                .andExpect(jsonPath("$.data.championRecords[0].episodeTitle").value("취업 최종 탈락"))
                .andExpect(jsonPath("$.data.championRecords[0].championTitle").value("올타임 챔피언 (All-Time Champion)"))
                .andExpect(jsonPath("$.data.matchRecords.length()").value(1))
                .andExpect(jsonPath("$.data.matchRecords[0].episodeATitle").value("취업 최종 탈락"))
                .andExpect(jsonPath("$.data.matchRecords[0].episodeAResult").value("WIN"))
                .andExpect(jsonPath("$.data.matchRecords[0].episodeBResult").value("LOSS"));
    }

    @Test
    void historySearchMatchesEpisodeTitleAndContentWithoutLeakingOtherMembersRecords() throws Exception {
        Episode searched = episodes.save(new Episode(member, "면접 기억", "최종 발표 날이었다.", LocalDate.of(2026, 7, 10)));
        Episode otherOwn = episodes.save(new Episode(member, "다른 기억", "일상적인 사건", LocalDate.of(2026, 7, 8)));
        rankings.save(RankingEpisodeScore.builder().episodeId(searched.getId()).titleScore(1200L).build());
        rankings.save(RankingEpisodeScore.builder().episodeId(otherOwn.getId()).titleScore(1100L).build());
        matches.save(completedMatch(searched, otherOwn, searched));

        Member another = members.save(new Member(
                "history-other@example.com",
                passwordEncoder.encode("password123!"),
                "다른 사용자"));
        Episode leaked = episodes.save(new Episode(another, "최종 발표", "검색되면 안 되는 기록", LocalDate.of(2026, 7, 7)));
        rankings.save(RankingEpisodeScore.builder().episodeId(leaked.getId()).titleScore(2000L).build());

        mockMvc.perform(get("/api/v1/history/champions")
                        .header("Authorization", bearer())
                        .param("query", "최종"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].episodeTitle").value("면접 기억"));

        mockMvc.perform(get("/api/v1/history/matches")
                        .header("Authorization", bearer())
                        .param("query", "최종"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].winnerEpisodeId").value(searched.getId()));
    }

    private Match completedMatch(Episode episodeA, Episode episodeB, Episode winner) {
        LocalDateTime completedAt = LocalDateTime.of(2026, 7, 11, 9, 30);
        return Match.builder()
                .memberId(member.getId())
                .episodeAId(episodeA.getId())
                .episodeBId(episodeB.getId())
                .winnerEpisodeId(winner.getId())
                .status("COMPLETED")
                .startedAt(completedAt.minusMinutes(5))
                .completedAt(completedAt)
                .build();
    }

    private String bearer() {
        return "Bearer " + token;
    }
}
