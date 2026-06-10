package sre.engine.strategy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import common.lib.schemaobjects.Registry;

@Configuration
public class BeanConfig {

    @Bean
    public Registry<Strategy> strategyRegistry() {
        return new Registry<>();
    }

    @Bean
    public Registry<TradingRecord> tradingRecordRegistry() {
        return new Registry<>();
    }
}
