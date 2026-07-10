package com.team6.server.global.config;

import com.team6.server.auth.repository.MemberRepository;
import com.team6.server.episode.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.match.entity.MatchingEvent;
import com.team6.server.match.repository.MatchingEventRepository;
import com.team6.server.member.Member;
import com.team6.server.ranking.entity.RankingEpisodeScore;
import com.team6.server.ranking.entity.Title;
import com.team6.server.ranking.repository.RankingEpisodeScoreRepository;
import com.team6.server.ranking.repository.TitleRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
@ConditionalOnProperty(prefix = "app.dummy-data", name = "enabled", havingValue = "true")
public class LocalDummyDataSeeder implements ApplicationRunner {
    public static final String PASSWORD = "password123!";
    public static final String ONBOARDING_EMAIL = "onboarding-demo@mme.local";
    public static final String LEAGUE_EMAIL = "league-demo@mme.local";

    private final MemberRepository members;
    private final EpisodeRepository episodes;
    private final RankingEpisodeScoreRepository rankings;
    private final TitleRepository titles;
    private final MatchingEventRepository events;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public LocalDummyDataSeeder(MemberRepository members, EpisodeRepository episodes,
                                RankingEpisodeScoreRepository rankings, TitleRepository titles,
                                MatchingEventRepository events, PasswordEncoder passwordEncoder, Clock clock) {
        this.members = members; this.episodes = episodes; this.rankings = rankings; this.titles = titles;
        this.events = events; this.passwordEncoder = passwordEncoder; this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Title> titleTiers = seedTitles();
        seedOnboardingMember(titleTiers);
        seedLeagueMember(titleTiers);
        seedShows();
    }

    private List<Title> seedTitles() {
        if (titles.count() == 0) {
            titles.saveAll(List.of(
                    Title.builder().code("ROOKIE").name("루키").description("리그 입문 칭호")
                            .minScore(0L).displayOrder(1).build(),
                    Title.builder().code("CONTENDER").name("도전자").description("상위권 도전자")
                            .minScore(1100L).displayOrder(2).build(),
                    Title.builder().code("CHAMPION").name("챔피언").description("MME 챔피언")
                            .minScore(1300L).displayOrder(3).build()));
        }
        return titles.findAllByOrderByMinScoreAsc();
    }

    private void seedOnboardingMember(List<Title> titleTiers) {
        if (members.findByEmail(ONBOARDING_EMAIL).isPresent()) return;
        Member member = members.save(new Member(ONBOARDING_EMAIL, passwordEncoder.encode(PASSWORD), "온보딩 데모"));
        member.startEpisodeRegistration();
        createEpisodes(member, List.of(
                "면접에서 얼어붙은 날", "발표 자료를 잘못 보낸 날", "지각해서 모두를 기다리게 한 날",
                "회의에서 이름을 잘못 부른 날", "시험 날짜를 착각한 날"), false,
                List.of(0L, 0L, 0L, 0L, 0L), titleTiers);
    }

    private void seedLeagueMember(List<Title> titleTiers) {
        if (members.findByEmail(LEAGUE_EMAIL).isPresent()) return;
        Member member = members.save(new Member(LEAGUE_EMAIL, passwordEncoder.encode(PASSWORD), "리그 데모"));
        member.completeOnboarding(LocalDateTime.now(clock).minusDays(30));
        createEpisodes(member, List.of(
                "최종 면접 탈락", "프로젝트 배포 실패", "중요한 약속을 잊은 날",
                "전사 발표에서 말문이 막힌 날", "잘못된 단체 채팅방에 보낸 메시지", "여행 비행기를 놓친 날"),
                true, List.of(450L, 300L, 180L, 70L, -50L, -150L), titleTiers);
    }

    private void createEpisodes(Member member, List<String> names, boolean placementCompleted,
                                List<Long> deltas, List<Title> titleTiers) {
        for (int i = 0; i < names.size(); i++) {
            Episode episode = episodes.save(new Episode(member, names.get(i),
                    names.get(i) + "에 대한 데모 회고 내용입니다.", LocalDate.now(clock).minusDays(i + 1L)));
            if (placementCompleted) {
                episode.startPlacement();
                episode.completePlacement();
            }
            RankingEpisodeScore ranking = RankingEpisodeScore.initial(episode.getId());
            ranking.applyDelta(deltas.get(i));
            Title tier = null;
            for (Title candidate : titleTiers) if (ranking.getTitleScore() >= candidate.getMinScore()) tier = candidate;
            ranking.updateTitle(tier);
            rankings.save(ranking);
        }
    }

    private void seedShows() {
        LocalDateTime now = LocalDateTime.now(clock);
        seedShow("WEEKLY", "DEMO-WEEKLY", "Monday Night Rivals (Demo)", "RIVAL", 5,
                now.minusDays(1), now.plusDays(30));
        seedShow("MONTHLY", "DEMO-MONTHLY", "Monthly Royal Rumble (Demo)", "MONTHLY_RUMBLE", 10,
                now.minusDays(1), now.plusDays(30));
    }

    private void seedShow(String type, String periodKey, String title, String matchType, int rounds,
                          LocalDateTime startsAt, LocalDateTime endsAt) {
        if (events.findByEventTypeAndPeriodKey(type, periodKey).isPresent()) return;
        events.save(MatchingEvent.builder().eventType(type).periodKey(periodKey).title(title)
                .description("로컬 API 테스트용 Show").matchType(matchType).startsAt(startsAt).endsAt(endsAt)
                .status("OPEN").scoreReward(0L).roundCount(rounds).scoreMultiplier(BigDecimal.ONE).build());
    }
}
