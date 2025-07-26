package money.maker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.context.annotation.Bean;
import money.maker.config.external.SchedulerConfiguration;

import java.util.concurrent.Executor;

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

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(8);
        threadPoolTaskExecutor.setThreadNamePrefix("exec-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}