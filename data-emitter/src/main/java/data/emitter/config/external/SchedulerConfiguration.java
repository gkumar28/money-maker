package data.emitter.config.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfiguration {

    private boolean connectAuto;
    private String openCron;
    private String closeCron;
    private String taskCron;

}
