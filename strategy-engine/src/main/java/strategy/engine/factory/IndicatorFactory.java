package strategy.engine.factory;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import strategy.engine.constant.IndicatorParam;
import strategy.engine.indicator.ScaledIndicator;
import strategy.engine.schemaobject.strategy.tree.IndicatorDefinition;

@Slf4j
public class IndicatorFactory {

    private final BarSeries series;
    private final Num numProvider;

    public IndicatorFactory(BarSeries series, Num numProvider) {
        this.series = series;
        this.numProvider = numProvider;
    }

    public Indicator<Num> create(IndicatorDefinition def) {
        return switch (def.getIndicatorType()) {

            case CLOSE_PRICE -> new ClosePriceIndicator(series);
            case HIGH_PRICE -> new HighPriceIndicator(series);
            case LOW_PRICE -> new LowPriceIndicator(series);
            case VOLUME -> new VolumeIndicator(series);
            case CONSTANT -> {
                Num value = numProvider.numOf((Number) def.getParameters().get(IndicatorParam.VALUE));
                yield new ConstantIndicator<>(series, value);
            }

            case EMA -> {
                Indicator<Num> base = create(def.getInputs().get(0));
                int barCount = (int) def.getParameters().get(IndicatorParam.BAR_COUNT);
                yield new EMAIndicator(base, barCount);
            }

            case RSI -> {
                Indicator<Num> base = create(def.getInputs().get(0));
                int barCount = (int) def.getParameters().get(IndicatorParam.BAR_COUNT);
                yield new RSIIndicator(base, barCount);
            }

            case ATR -> {
                int barCount = (int) def.getParameters().get(IndicatorParam.BAR_COUNT);
                yield new ATRIndicator(series, barCount);
            }

            case ADX -> {
                int barCount = (int) def.getParameters().get(IndicatorParam.BAR_COUNT);
                yield new ADXIndicator(series, barCount);
            }
            case MACD -> {
                Indicator<Num> base = create(def.getInputs().get(0));
                int shortBarCount = (int) def.getParameters().get(IndicatorParam.SHORT_BAR_COUNT);
                int longBarCount = (int) def.getParameters().get(IndicatorParam.LONG_BAR_COUNT);
                yield new MACDIndicator(base, shortBarCount, longBarCount);
            }

            case MACD_SIGNAL -> {
                MACDIndicator macd = (MACDIndicator) create(def.getInputs().get(0));
                int signalBarCount = (int) def.getParameters().get(IndicatorParam.SIGNAL_BAR_COUNT);
                yield macd.getSignalLine(signalBarCount);
            }

            case SCALED -> {
                Indicator<Num> base = create(def.getInputs().get(0));
                Num scale = numProvider.numOf((Number) def.getParameters().get(IndicatorParam.SCALE));
                yield new ScaledIndicator(base, scale);
            }

            case HIGHEST_VALUE -> {
                Indicator<Num> base = create(def.getInputs().get(0));
                int barCount = (int) def.getParameters().get(IndicatorParam.BAR_COUNT);
                yield new HighestValueIndicator(base, barCount);
            }

            case RECENT_SWING_HIGH -> {
                Indicator<Num> high = create(def.getInputs().get(0));
                int equal = (int) def.getParameters().get(IndicatorParam.ALLOWED_EQUAL_BARS);
                int preceding = (int) def.getParameters().get(IndicatorParam.PRECEDING_LOWER_BARS);
                int following = (int) def.getParameters().get(IndicatorParam.FOLLOWING_LOWER_BARS);
                yield new RecentSwingHighIndicator(high, preceding, following, equal);
            }

            default -> throw new IllegalArgumentException("Unsupported indicator type: " + def.getIndicatorType());
        };
    }
}
