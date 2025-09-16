package strategy.engine.util;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradeDto;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
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

    public static BigDecimal roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static TradeDto convertOrderToTrade(StrategyOrderDto order, int atIndex) {
        TradeDto tradeDto = new TradeDto();
        tradeDto.setInstrument(order.getInstrument());
        tradeDto.setQuantity(order.getQuantity());
        tradeDto.setPrice(order.getPrice());
        tradeDto.setDirection(order.getDirection());
        tradeDto.setIndex(atIndex);
        tradeDto.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
        return tradeDto;
    }

    public static TradeDto mergeTrades(TradeDto... trades) {
        if (trades == null || trades.length == 0) {
            throw new IllegalArgumentException("Number of trades must be greater than 0");
        }

        // Find the first non-null trade to get instrument/direction
        TradeDto firstNonNull = Arrays.stream(trades)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("All trades are null"));

        String instrument = firstNonNull.getInstrument();
        TradeDirection direction = firstNonNull.getDirection();

        int totalQuantity = 0;
        BigDecimal totalWeightedPrice = BigDecimal.ZERO;
        int lastIndex = 0;

        for (TradeDto trade : trades) {
            if (trade == null) continue;

            if (!instrument.equals(trade.getInstrument())) {
                throw new IllegalArgumentException("Cannot merge trades with different instruments");
            }
            if (direction != trade.getDirection()) {
                throw new IllegalArgumentException("Cannot merge trades with different directions");
            }

            BigDecimal qtyBD = BigDecimal.valueOf(trade.getQuantity());
            totalWeightedPrice = totalWeightedPrice.add(trade.getPrice().multiply(qtyBD));
            totalQuantity += trade.getQuantity();
            lastIndex = Math.max(lastIndex, trade.getIndex());
        }

        if (totalQuantity == 0) {
            throw new IllegalArgumentException("Total quantity is zero. Cannot calculate average price.");
        }

        BigDecimal averagePrice = totalWeightedPrice
            .divide(BigDecimal.valueOf(totalQuantity), 6, RoundingMode.HALF_UP);

        TradeDto merged = new TradeDto();
        merged.setInstrument(instrument);
        merged.setDirection(direction);
        merged.setQuantity(totalQuantity);
        merged.setPrice(averagePrice);
        merged.setIndex(lastIndex);

        return merged;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public static Trade asTrade(TradeDto tradeDto) {
        if (tradeDto.getDirection().name().equalsIgnoreCase(Trade.TradeType.BUY.toString())) {
            return Trade.buyAt(tradeDto.getIndex(), DecimalNum.valueOf(tradeDto.getPrice()), DecimalNum.valueOf(tradeDto.getQuantity()));
        }
        return Trade.sellAt(tradeDto.getIndex(), DecimalNum.valueOf(tradeDto.getPrice()), DecimalNum.valueOf(tradeDto.getQuantity()));
    }

    public static Trade.TradeType asTradeType(TradeDirection direction) {
        if (null == direction) {
            return null;
        }

        if (Trade.TradeType.BUY.toString().equalsIgnoreCase(direction.toString())) {
            return Trade.TradeType.BUY;
        }

        return Trade.TradeType.SELL;
    }

    public static TradeDirection asTradeDirection(Trade.TradeType tradeType) {
        if (null == tradeType) {
            return null;
        }

        if (Trade.TradeType.BUY.toString().equalsIgnoreCase(tradeType.toString())) {
            return TradeDirection.BUY;
        }

        return TradeDirection.SELL;
    }

}
