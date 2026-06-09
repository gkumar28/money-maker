package sre.engine.strategy.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
