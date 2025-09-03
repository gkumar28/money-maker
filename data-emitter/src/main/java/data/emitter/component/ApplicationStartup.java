package data.emitter.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import data.emitter.config.external.SchedulerConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

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
    }

    private void scheduleTask(String cronExp, Runnable task) {
        if (StringUtils.isNotBlank(cronExp)) {
            CronTrigger trigger = new CronTrigger(cronExp);
            threadPoolTaskScheduler.schedule(task, trigger);
        }
    }
}
