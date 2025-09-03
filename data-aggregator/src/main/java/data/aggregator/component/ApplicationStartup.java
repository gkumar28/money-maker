package data.aggregator.component;

import data.aggregator.config.external.SchedulerConfiguration;
import data.aggregator.service.TickAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final SchedulerConfiguration schedulerConfiguration;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final TickAggregatorService tickAggregatorService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        threadPoolTaskScheduler.schedule(tickAggregatorService::updateInstrument,
            new CronTrigger(schedulerConfiguration.getTaskCron()));
    }
}
