package money.maker.config.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "candle")
public class CandleConfiguration {

    private long aggregationPeriod;

    private MAConfig ma = new MAConfig();
    private RSIConfig rsi = new RSIConfig();

    @Data
    public static class MAConfig {
        private int frameCountLong;
        private int frameCountShort;
    }

    @Data
    public static class RSIConfig {
        private int frameCount;
    }
}
