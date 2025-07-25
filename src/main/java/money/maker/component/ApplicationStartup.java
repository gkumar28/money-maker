package money.maker.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.config.external.CandleConfiguration;
import money.maker.config.external.SchedulerConfiguration;
import money.maker.service.CandleAggregatorService;
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

    private final CandleConfiguration candleConfiguration;
    private final CandleAggregatorService candleAggregatorService;
    private final SchedulerConfiguration schedulerConfiguration;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final OAuthWebSocketClient webSocketClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {


        // schedule task for opening wb
        scheduleTask(schedulerConfiguration.getOpenCron(), webSocketClient::connect);

        // schedule task for closing wb
        scheduleTask(schedulerConfiguration.getCloseCron(), webSocketClient::close);

        // start trade signal task
        // to be implemented

        // start candle aggregation task
        long aggregationPeriod = candleConfiguration.getAggregationPeriod();
        int longPeriod = candleConfiguration.getMa().getFrameCountLong();
        int shortPeriod = candleConfiguration.getMa().getFrameCountShort();
        int rsiPeriod = candleConfiguration.getRsi().getFrameCount();

        threadPoolTaskScheduler.scheduleAtFixedRate(() ->
            candleAggregatorService.updateInstrument(longPeriod, shortPeriod, rsiPeriod),
            Duration.of(aggregationPeriod, ChronoUnit.SECONDS));
    }

    private void scheduleTask(String cronExp, Runnable task) {
        if (StringUtils.isNotBlank(cronExp)) {
            CronTrigger trigger = new CronTrigger(cronExp);
            threadPoolTaskScheduler.schedule(task, trigger);
        }
    }
}
