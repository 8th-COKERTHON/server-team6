package com.team6.server.match.service;

import com.team6.server.match.entity.MatchingEvent;
import com.team6.server.match.repository.MatchingEventRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShowScheduler {
    private final MatchingEventRepository events;
    private final Clock clock;

    public ShowScheduler(MatchingEventRepository events, Clock clock) {
        this.events = events;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void createWeekly() {
        LocalDate today = LocalDate.now(clock);
        createWeekly(today);
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void createMonthlyAndCloseExpired() {
        LocalDate today = LocalDate.now(clock);
        if (today.equals(today.withDayOfMonth(today.lengthOfMonth()))) createMonthly(today);
        LocalDateTime now = LocalDateTime.now(clock);
        events.findByStatus("OPEN").stream().filter(event -> !event.getEndsAt().isAfter(now))
                .forEach(MatchingEvent::close);
    }

    void createWeekly(LocalDate date) {
        WeekFields fields = WeekFields.ISO;
        String periodKey = date.get(fields.weekBasedYear()) + "-W"
                + String.format("%02d", date.get(fields.weekOfWeekBasedYear()));
        if (events.findByEventTypeAndPeriodKey("WEEKLY", periodKey).isPresent()) return;
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        events.save(event("WEEKLY", periodKey, "Monday Night Rivals", "RIVAL", 5,
                monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()));
    }

    void createMonthly(LocalDate date) {
        String periodKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        if (events.findByEventTypeAndPeriodKey("MONTHLY", periodKey).isPresent()) return;
        LocalDate start = date.withDayOfMonth(date.lengthOfMonth());
        events.save(event("MONTHLY", periodKey, "Monthly Royal Rumble", "MONTHLY_RUMBLE", 10,
                start.atStartOfDay(), start.plusDays(1).atStartOfDay()));
    }

    private MatchingEvent event(String eventType, String periodKey, String title, String matchType,
                                int rounds, LocalDateTime startsAt, LocalDateTime endsAt) {
        return MatchingEvent.builder().eventType(eventType).periodKey(periodKey).title(title)
                .description(title + " 정기 대결").matchType(matchType).startsAt(startsAt).endsAt(endsAt)
                .status("OPEN").scoreReward(0L).roundCount(rounds).scoreMultiplier(BigDecimal.ONE).build();
    }
}
