package strategy.engine.util;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTrade;
import org.ta4j.core.Trade;
import strategy.engine.schemaobject.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class StrategyEngineUtils {

    private StrategyEngineUtils() {}

    public static double normalize(double val, double min, double max) {
        return Math.clamp((val - min) / (max - min), 0.0, 1.0);
    }

    public static BigDecimal normalize(BigDecimal val, BigDecimal min, BigDecimal max) {
        return BigDecimal.ZERO.max(BigDecimal.ONE.min(val.subtract(min).divide(max.subtract(min), 4, RoundingMode.HALF_UP)));
    }

    public static BigDecimal sanitize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

}
