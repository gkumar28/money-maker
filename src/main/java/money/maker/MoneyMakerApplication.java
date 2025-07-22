package money.maker;

import money.maker.config.external.CandleConfiguration;
import money.maker.config.external.SchedulerConfiguration;
import money.maker.config.external.WebSocketClientConfiguration;
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
    CandleConfiguration.class,
    SchedulerConfiguration.class
})
public class MoneyMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyMakerApplication.class, args);
    }

}