package strategy.engine.strategy.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.OrRule;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.rule.DecayRule;
import strategy.engine.rule.SurgeRule;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.strategy.TradingStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import strategy.engine.util.StrategyEngineUtils;

import java.math.BigDecimal;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class LongTrendStrategy extends TradingStrategy {

    private ATRIndicator atr;
    private ADXIndicator adx;
    private SMAIndicator avgVolume;
    private RSIIndicator rsi;
    private MACDIndicator macd;
    private EMAIndicator macdSignal;
    private LowestValueIndicator recentLow;
    private PlusDIIndicator plusDI;
    private MinusDIIndicator minusDI;
    private ClosePriceIndicator close;
    private KallmanIndicator kallman;

    public LongTrendStrategy(BarSeries barSeries) {
        super(barSeries);
    }

    @Override
    protected void build() {

        atr = new ATRIndicator(barSeries, 14);
        adx = new ADXIndicator(barSeries, 14);
        close = new ClosePriceIndicator(barSeries);
        rsi = new RSIIndicator(close, 14);
        VolumeIndicator volume = new VolumeIndicator(barSeries);
        avgVolume = new SMAIndicator(volume, 20);
        kallman = new KallmanIndicator(close, 0.01, 20);
        macd = new MACDIndicator(close, 12, 26);
        macdSignal = new EMAIndicator(macd, 9);

        plusDI = new PlusDIIndicator(barSeries, 14);
        minusDI = new MinusDIIndicator(barSeries, 14);
        recentLow = new LowestValueIndicator(new LowPriceIndicator(barSeries), 5);

        // Entry rule - shows bullish direction
        Rule entryRule = new SurgeRule(kallman,
            new ConstantIndicator<>(barSeries, DecimalNum.valueOf(0.3)),
            DecimalNum.valueOf(1),
            DecimalNum.valueOf(5))
            .and(new CrossedUpIndicatorRule(macd, macdSignal));

        // Exit rule - only basis exit strength
        Rule exitRule = new OrRule(
                new CrossedDownIndicatorRule(rsi, DecimalNum.valueOf(65)),
                new CrossedUpIndicatorRule(minusDI, plusDI)
                .and(new DecayRule(kallman,
                    new ConstantIndicator<>(barSeries, DecimalNum.valueOf(-0.1)),
                    DecimalNum.valueOf(1),
                    DecimalNum.valueOf(5)))
        );

        this.strategy =  new BaseStrategy(this.getClass().getName(), entryRule, exitRule, 5);
    }

    @Override
    public SignalDto evaluate(int index) {

        boolean shouldEnter = strategy.shouldEnter(index);
        boolean shouldExit = strategy.shouldExit(index);

        BigDecimal atrValue = atr.getValue(index).bigDecimalValue();
        BigDecimal adx = avgVolume.getValue(index).bigDecimalValue();

        // ---- Signal Strength Logic ----
        BigDecimal entry = shouldEnter ? calculateEntryStrength(index) : BigDecimal.ZERO;
        BigDecimal exit =  shouldExit ? calculateExitStrength(index) : BigDecimal.ZERO;

        // normalized parameters
        BigDecimal normalizedEntry;
        BigDecimal normalizedExit;
        BigDecimal normalizedHold;

        if (entry.add(exit).compareTo(BigDecimal.ONE) >= 0) {
            normalizedEntry = StrategyEngineUtils.normalize(entry, BigDecimal.ZERO, entry.add(exit));
            normalizedExit = StrategyEngineUtils.normalize(exit, BigDecimal.ZERO, entry.add(exit));
            normalizedHold = BigDecimal.ZERO;
        } else {
            normalizedEntry = entry;
            normalizedExit = exit;
            normalizedHold = BigDecimal.ONE.subtract(entry).subtract(exit);
        }

        TradeDirection tradeDirection;
        BigDecimal confidence;

        if (shouldEnter) {
            tradeDirection = TradeDirection.BUY;
            confidence = normalizedEntry;
            logDebugMessage("ENTRY", index, confidence);
        } else if (shouldExit) {
            tradeDirection = TradeDirection.SELL;
            confidence = normalizedExit;
            logDebugMessage("EXIT", index, confidence);
        } else {
            tradeDirection = null;
            confidence = normalizedHold;
        }

        return new SignalDto(
            tradeDirection,
            confidence,
            barSeries.getBar(index).getEndTime(),
            barSeries.getBar(index).getClosePrice().bigDecimalValue(),
            atrValue,
            adx
        );
    }

    private void logDebugMessage(String signal, int index, BigDecimal confidence) {
        if (log.isDebugEnabled()) {
            log.debug("{}: {} with confidence {}", barSeries.getName(), signal, confidence);
        }
    }

    private BigDecimal calculateEntryStrength(int index) {
        double atrRatio = atr.getValue(index).doubleValue() / close.getValue(index).doubleValue();
        double atrScore = 1.0 - Math.min(atrRatio, 0.05) / 0.05; // Max out at 5% ATR

        double adxVal = adx.getValue(index).doubleValue();
        double adxScore = StrategyEngineUtils.normalize(adxVal, 20, 50); // Stronger trend = higher score

        double rsiVal = rsi.getValue(index).doubleValue();
        double rsiScore = 0.0;

        // Assume 35–65 is the optimal "entry zone"
        if (rsiVal >= 35 && rsiVal <= 65) {
            rsiScore = 1.0; // Perfect zone
        } else if (rsiVal < 35) {
            rsiScore = StrategyEngineUtils.normalize(rsiVal, 20, 35); // 20-35 maps to 0–1
        } else {
            rsiScore = StrategyEngineUtils.normalize(65 - rsiVal, 0, 15); // 65–80 maps to 1–0
        }

        double weightedScore = 0.4 * atrScore + 0.4 * adxScore + 0.2 * rsiScore;
        return StrategyEngineUtils.roundToTwoDecimals(weightedScore);
    }

    private BigDecimal calculateExitStrength(int index) {
        double strength = 0.0;
        int totalWeight = 0;

        // ========== Branch 1: Trend Weakening ==========
        boolean adxDrop = adx.getValue(index - 1).isGreaterThan(DecimalNum.valueOf(25)) &&
            adx.getValue(index).isLessThan(DecimalNum.valueOf(25));
        boolean rsiDrop = rsi.getValue(index - 1).isGreaterThan(DecimalNum.valueOf(65)) &&
            rsi.getValue(index).isLessThan(DecimalNum.valueOf(65));
        boolean macdCrossDown = macd.getValue(index - 1).isGreaterThan(macdSignal.getValue(index - 1)) &&
            macd.getValue(index).isLessThan(macdSignal.getValue(index));

        if (adxDrop) {
            strength += 0.3;
            totalWeight += 1;
        }
        if (rsiDrop) {
            strength += 0.35;
            totalWeight += 1;
        }
        if (macdCrossDown) {
            strength += 0.3;
            totalWeight += 1;
        }

        // ========== Branch 2: Reversal ==========
        boolean priceBreakSupport = close.getValue(index - 1).isGreaterThan(recentLow.getValue(index - 1)) &&
            close.getValue(index).isLessThan(recentLow.getValue(index));
        boolean diCross = minusDI.getValue(index - 1).isLessThan(plusDI.getValue(index - 1)) &&
            minusDI.getValue(index).isGreaterThan(plusDI.getValue(index));
        boolean kallmanDecay = kallman.getValue(index).isLessThan(DecimalNum.valueOf(-0.2));

        if (priceBreakSupport) {
            strength += 1;
            totalWeight += 1;
        }
        if (diCross) {
            strength += 1;
            totalWeight += 1;
        }
        if (kallmanDecay) {
            strength += 0.5;
            totalWeight += 1;
        }

        // Normalize and round
        double normalized = totalWeight > 0 ? strength / totalWeight : 0.0;
        return StrategyEngineUtils.roundToTwoDecimals(normalized);
    }

}
