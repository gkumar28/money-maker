package strategy.engine;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import strategy.engine.config.external.BarConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    BarConfiguration.class,
})
@EnableFeignClients
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class StrategyEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(StrategyEngineApplication.class, args);
    }

}