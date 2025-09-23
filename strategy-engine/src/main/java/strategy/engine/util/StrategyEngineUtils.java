package strategy.engine.util;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StrategyEngineUtils {

    private StrategyEngineUtils() {}

    public static double normalize(double val, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
    }

    public static BigDecimal normalize(BigDecimal val, BigDecimal min, BigDecimal max) {
        return BigDecimal.ZERO.max(BigDecimal.ONE.min(val.subtract(min).divide(max.subtract(min), 4, RoundingMode.HALF_UP)));
    }

    public static Trade convertOrderToTrade(Order order, int atIndex, ZonedDateTime timestamp) {
        Trade trade = new Trade();
        trade.setInstrument(order.getInstrument());
        trade.setQuantity(order.getQuantity());
        trade.setPrice(order.getPrice());
        trade.setTradeType(order.getTradeType());
        trade.setIndex(atIndex);
        trade.setTimestamp(timestamp);
        return trade;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public static BigDecimal sanitize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

}
