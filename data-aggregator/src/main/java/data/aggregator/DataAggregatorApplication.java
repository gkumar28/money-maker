package data.aggregator;

import data.aggregator.config.external.SchedulerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    SchedulerConfiguration.class
})
public class DataAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAggregatorApplication.class, args);
    }

}