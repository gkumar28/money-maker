package sre.engine;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
@EnableJpaRepositories
@EnableFeignClients
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class SreEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(SreEngineApplication.class, args);
    }

}