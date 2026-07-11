package com.team6.server.global.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class TimeConfig {
    public static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    Clock serviceClock() {
        return Clock.system(SERVICE_ZONE);
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider(Clock serviceClock) {
        return () -> Optional.of(LocalDateTime.now(serviceClock));
    }
}
