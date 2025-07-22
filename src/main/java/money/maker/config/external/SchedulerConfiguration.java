package money.maker.config.external;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfiguration {

    private final String openCron;
    private final String closeCron;
    private final String taskCron;
    private final int poolSize;

}
