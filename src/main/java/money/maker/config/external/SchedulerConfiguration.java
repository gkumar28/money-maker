package money.maker.config.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfiguration {

    private final boolean connectAuto;
    private String openCron;
    private String closeCron;
    private final String taskCron;
    private final int poolSize;

}
