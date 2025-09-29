package strategy.engine.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.constant.IndicatorParam;
import strategy.engine.constant.enums.IndicatorType;
import strategy.engine.schemaobject.strategy.tree.IndicatorDefinition;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static strategy.engine.constant.enums.IndicatorType.ADX;
import static strategy.engine.constant.enums.IndicatorType.ATR;
import static strategy.engine.constant.enums.IndicatorType.CLOSE_PRICE;
import static strategy.engine.constant.enums.IndicatorType.EMA;
import static strategy.engine.constant.enums.IndicatorType.HIGH_PRICE;
import static strategy.engine.constant.enums.IndicatorType.LOW_PRICE;
import static strategy.engine.constant.enums.IndicatorType.RSI;
import static strategy.engine.constant.enums.IndicatorType.VOLUME;

@RequiredArgsConstructor
@Slf4j
public class IndicatorDefinitionGenerator {

    public IndicatorDefinition generate(Random rng) {
        IndicatorType[] possibleTypes = IndicatorType.values();
        return generate(possibleTypes[rng.nextInt(possibleTypes.length)], rng);
    }

    public IndicatorDefinition generate(IndicatorType indicatorType, Random rng) {
        Map<String, Object> parameters = null;
        List<IndicatorDefinition> inputs = null;
        if (List.of(CLOSE_PRICE, HIGH_PRICE, LOW_PRICE, VOLUME).contains(indicatorType)) {
            return new IndicatorDefinition(indicatorType, null, null);
        }

        if (IndicatorType.CONSTANT == indicatorType) {
            Number[] possibleValues = getRange(1, 2000);
            parameters = Map.of(IndicatorParam.VALUE, possibleValues[rng.nextInt(2000)]);
        }

        if (List.of(EMA, ADX, RSI, ATR).contains(indicatorType)) {
            Number[] barCount = new Number[]{4, 6, 8, 10, 12, 14, 16, 18, 20};
            parameters = Map.of(IndicatorParam.BAR_COUNT, barCount[rng.nextInt(barCount.length)]);
        }

        if (IndicatorType.MACD.equals(indicatorType)) {
            Number[] shortBarCount = new Number[]{5, 8, 12, 18};
            Number[] longBarCount = new Number[]{12, 18, 26, 38};
            int index = rng.nextInt(4);
            parameters = Map.of(IndicatorParam.SHORT_BAR_COUNT, shortBarCount[index],
                IndicatorParam.LONG_BAR_COUNT, longBarCount[index]);
        }

        if (IndicatorType.MACD_SIGNAL.equals(indicatorType)) {
            Number[] signalBarCount = new Number[]{2, 5, 9, 15};
            int index = rng.nextInt(4);
            parameters = Map.of(IndicatorParam.SIGNAL_BAR_COUNT, signalBarCount[index]);
        }

        if (IndicatorType.SCALED.equals(indicatorType)) {
            Number[] possibleValues = {0.5, 0.8, 1, 1.2, 1.4, 1.6, 1.8, 2};
            parameters = Map.of(IndicatorParam.SCALE, possibleValues[rng.nextInt(possibleValues.length)]);
        }

        if (IndicatorType.HIGHEST_VALUE.equals(indicatorType)) {
            IndicatorType[] baseIndicators = new IndicatorType[]{CLOSE_PRICE, LOW_PRICE, HIGH_PRICE};
            Number[] signalBarCount = new Number[]{2, 5, 9, 15};
            parameters = Map.of(IndicatorParam.SIGNAL_BAR_COUNT, signalBarCount[rng.nextInt(4)]);
            inputs = List.of(generate(baseIndicators[rng.nextInt(3)], rng));
        }

        if (IndicatorType.RECENT_SWING_HIGH.equals(indicatorType)) {
            IndicatorType[] baseIndicators = new IndicatorType[]{CLOSE_PRICE, LOW_PRICE, HIGH_PRICE};
            Number[] allowedBarCount = new Number[]{0, 1, 2, 5};
            Number[] precedingBarCount = new Number[]{2, 5, 10, 15};
            Number[] followingBarCount = new Number[]{1, 2, 5};
            inputs = List.of(generate(baseIndicators[rng.nextInt(3)], rng));
            parameters = Map.of(
                IndicatorParam.ALLOWED_EQUAL_BARS, allowedBarCount[rng.nextInt(4)],
                IndicatorParam.PRECEDING_LOWER_BARS, precedingBarCount[rng.nextInt(4)],
                IndicatorParam.FOLLOWING_LOWER_BARS, followingBarCount[rng.nextInt(3)]);
        }

        return new IndicatorDefinition(indicatorType, parameters, inputs);
    }

    private Number[] getRange(int start, int end) {
        Number[] result = new Number[2000];
        for(int i=start;i<=end;i++) {
            result[i] = i;
        }
        return result;
    }
}
