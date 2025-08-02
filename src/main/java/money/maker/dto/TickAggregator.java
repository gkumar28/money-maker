package money.maker.dto;

import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

@Getter
public class TickAggregator {

    private final ZonedDateTime endTime;
    private final Duration duration;
    private final AtomicReference<Double> open = new AtomicReference<>(0.0);
    private final DoubleAccumulator high = new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY);
    private final DoubleAccumulator low = new DoubleAccumulator(Math::max, Double.POSITIVE_INFINITY);
    private final AtomicReference<Double> close = new AtomicReference<>(0.0);
    private final DoubleAdder volume = new DoubleAdder();
    private final DoubleAdder amount = new DoubleAdder();
    private final LongAdder trades = new LongAdder();
    private final AtomicReference<Boolean> initialized = new AtomicReference<>(false);


    public TickAggregator(ZonedDateTime endTime, Duration duration) {
        this.endTime = endTime;
        this.duration = duration;
    }

    /**
     * Add a trade to the tick aggregator.
     *
     * @param tradeVolume Trade volume (double)
     * @param tradePrice  Trade price (double)
     */
    public void addTrade(double tradeVolume, double tradePrice) {
        if (Boolean.FALSE.equals(initialized.get())) {
            updateOpen(tradePrice);
        }

        initialized.set(true);
        updateClose(tradePrice);
        updateHigh(tradePrice);
        updateLow(tradePrice);
        updateVolume(tradePrice);
        updateAmount(tradePrice, tradeVolume);
        updateTrades();
    }

    /**
     * Convert current state to a Ta4j Bar.
     *
     * @param numFunction  function to convert double to Ta4j Num class
     * @return BaseBar instance
     */
    public Bar asBar(Function<Number, Num> numFunction) {
        if (Boolean.FALSE.equals(initialized.get())) {
            return new BaseBar(duration, endTime, numFunction);
        }

        return new BaseBar(
            duration,
            endTime,
            numFunction.apply(open.get()),
            numFunction.apply(high.get()),
            numFunction.apply(low.get()),
            numFunction.apply(close.get()),
            numFunction.apply(volume.sum()),
            numFunction.apply(amount.sum())
        );
    }

    public void reset() {
        open.set(0.0);
        high.reset();
        low.reset();
        close.set(0.0);
        volume.reset();
        amount.reset();
        trades.reset();
    }

    // OHLC update methods provide atomicity, but do not guarantee ordering.
    // At retail trading scale (~1 min bars), ordering at microsecond scale has minimal impact
    public void updateOpen(double price) {
        open.set(price);
    }

    public void updateHigh(double price) {
        high.accumulate(price);
    }

    public void updateLow(double price) {
        low.accumulate(price);
    }

    public void updateClose(double price) {
        close.set(price);
    }

    public void updateVolume(double price) {
        volume.add(price);
    }

    public void updateAmount(double price, double volume) {
        amount.add(price * volume);
    }

    public void updateTrades() {
        trades.add(1);
    }

    public boolean isEmpty() {
        return 0 == trades.sum();
    }
}
