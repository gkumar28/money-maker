package money.maker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.context.annotation.Bean;
import money.maker.config.external.SchedulerConfiguration;

@Configuration
@RequiredArgsConstructor
public class ThreadConfig {

    private final SchedulerConfiguration schedulerConfiguration;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerConfiguration.getPoolSize());
        scheduler.setThreadNamePrefix("scheduled-");

        return scheduler;
    }
}