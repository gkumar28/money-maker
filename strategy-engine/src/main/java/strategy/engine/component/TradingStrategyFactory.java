package strategy.engine.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.strategy.TradingStrategy;
import strategy.engine.strategy.impl.LongTrendStrategy;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TradingStrategyFactory {

    public TradingStrategy create(StrategyType type, BarSeries barSeries) {
        TradingStrategy strategy;

        if (Objects.requireNonNull(type) == StrategyType.LONG_TREND) {
            strategy = new LongTrendStrategy(barSeries);
        } else {
            throw new IllegalArgumentException("Unknown strategy");
        }

        return strategy;
    }
}
