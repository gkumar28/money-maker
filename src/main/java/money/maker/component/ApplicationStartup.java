package money.maker.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.config.external.CandleConfiguration;
import money.maker.config.external.SchedulerConfiguration;
import money.maker.service.CandleAggregatorService;
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

        // start websocket and taskRunner
        CronTrigger dailyStartTrigger = new CronTrigger(schedulerConfiguration.getOpenCron());
        webSocketClient.connect();
        threadPoolTaskScheduler.schedule(webSocketClient::connect, dailyStartTrigger);

        // stop websocket and taskRunner
        CronTrigger dailyStopTrigger = new CronTrigger(schedulerConfiguration.getCloseCron());
        threadPoolTaskScheduler.schedule(webSocketClient::close, dailyStopTrigger);

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
}
