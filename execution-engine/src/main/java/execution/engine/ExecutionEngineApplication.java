package execution.engine;

import execution.engine.config.external.BarConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    BarConfiguration.class,
})
@EntityScan(basePackages = "execution.engine.entity")
@EnableJpaRepositories(basePackages = "execution.engine.repository")
public class ExecutionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutionEngineApplication.class, args);
    }

}