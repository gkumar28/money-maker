package data.emitter;

import data.emitter.config.external.SchedulerConfiguration;
import data.emitter.config.external.WebSocketClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    WebSocketClientConfiguration.class,
    SchedulerConfiguration.class
})
public class DataEmitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataEmitterApplication.class, args);
    }

}