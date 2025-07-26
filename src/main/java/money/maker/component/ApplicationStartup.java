package money.maker.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.config.external.BarConfiguration;
import money.maker.config.external.SchedulerConfiguration;
import money.maker.service.TickAggregatorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final BarConfiguration barConfiguration;
    private final TickAggregatorService tickAggregatorService;
    private final SchedulerConfiguration schedulerConfiguration;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final OAuthWebSocketClient webSocketClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        if (schedulerConfiguration.isConnectAuto()) {
            webSocketClient.connect();
        }

        // schedule task for opening wb
        scheduleTask(schedulerConfiguration.getOpenCron(), webSocketClient::connect);

        // schedule task for closing wb
        scheduleTask(schedulerConfiguration.getCloseCron(), webSocketClient::close);

        // start trade signal task
        // to be implemented

        // start candle aggregation task
        long timeFrame = barConfiguration.getTimeFrame();

        threadPoolTaskScheduler.scheduleAtFixedRate(tickAggregatorService::updateInstrument,
            Duration.of(timeFrame, ChronoUnit.SECONDS));
    }

    private void scheduleTask(String cronExp, Runnable task) {
        if (StringUtils.isNotBlank(cronExp)) {
            CronTrigger trigger = new CronTrigger(cronExp);
            threadPoolTaskScheduler.schedule(task, trigger);
        }
    }
}
