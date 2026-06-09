package sre.engine.strategy.config.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bar")
public class BarConfiguration {

    private int timeFrame;
}
