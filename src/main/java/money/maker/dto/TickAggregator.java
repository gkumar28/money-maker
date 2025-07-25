package money.maker.dto;

import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

@Getter
public class TickAggregator {

    private double open = 0;
    private double high = Double.NEGATIVE_INFINITY;
    private double low = Double.POSITIVE_INFINITY;
    private double close = 0;
    private double volume = 0;
    private double amount = 0; // price * volume
    private long trades = 0l;
    private boolean initialized = false;

    private final Function<Number, Num> numFunction;

    public TickAggregator(Function<Number, Num> numFunction) {
        this.numFunction = numFunction;
    }

    /**
     * Add a trade to the tick aggregator.
     *
     * @param tradeVolume Trade volume (double)
     * @param tradePrice  Trade price (double)
     */
    public void addTrade(double tradeVolume, double tradePrice) {
        if (!initialized) {
            open = tradePrice;
            high = tradePrice;
            low = tradePrice;
            close = tradePrice;
            initialized = true;
        } else {
            if (tradePrice > high) high = tradePrice;
            if (tradePrice < low)  low = tradePrice;
            close = tradePrice;
        }

        volume += tradeVolume;
        amount += tradePrice * tradeVolume;
        trades++;
    }

    /**
     * Convert current state to a Ta4j Bar.
     *
     * @param endTime  The bar's end time
     * @param duration The bar's duration
     * @return BaseBar instance
     */
    public Bar asBar(ZonedDateTime endTime, Duration duration) {
        if (!initialized) {
            return new BaseBar(duration, endTime, numFunction);
        }

        return new BaseBar(
            duration,
            endTime,
            numFunction.apply(open),
            numFunction.apply(high),
            numFunction.apply(low),
            numFunction.apply(close),
            numFunction.apply(volume),
            numFunction.apply(amount),
            trades
        );
    }

    public void reset() {
        open = 0;
        high = Double.NEGATIVE_INFINITY;
        low = Double.POSITIVE_INFINITY;
        close = 0;
        volume = 0;
        amount = 0;
        trades = 0;
        initialized = false;
    }
}
