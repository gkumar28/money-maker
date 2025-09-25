package strategy.engine.factory;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AndRule;
import org.ta4j.core.rules.AverageTrueRangeStopGainRule;
import org.ta4j.core.rules.AverageTrueRangeStopLossRule;
import org.ta4j.core.rules.AverageTrueRangeTrailingStopLossRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.IsEqualRule;
import org.ta4j.core.rules.IsFallingRule;
import org.ta4j.core.rules.IsRisingRule;
import org.ta4j.core.rules.NotRule;
import org.ta4j.core.rules.OrRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.TrailingStopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.rules.XorRule;
import strategy.engine.constant.RuleParam;
import strategy.engine.constant.enums.LeafRuleType;
import strategy.engine.constant.enums.LogicalRuleType;
import strategy.engine.rule.WithinPercentageRule;
import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LeafRuleSignature;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleSignature;

import java.util.List;

@Slf4j
public class RuleFactory {

    private final BarSeries barSeries;
    private final IndicatorFactory indicatorFactory;

    public RuleFactory(BarSeries barSeries, IndicatorFactory indicatorFactory) {
        this.barSeries = barSeries;
        this.indicatorFactory = indicatorFactory;
    }

    public Rule create(RuleDefinition ruleDefinition) {
        if (ruleDefinition instanceof LogicalRuleDefinition logicalRule) {
            return createLogicalRule(logicalRule);
        } else if (ruleDefinition instanceof LeafRuleDefinition leafRule) {
            return createLeafRule(leafRule);
        } else {
            throw new IllegalArgumentException("Unknown RuleDefinition subtype: " + ruleDefinition.getClass());
        }
    }

    private Rule createLogicalRule(LogicalRuleDefinition logicalRuleDefinition) {
        LogicalRuleType type = (LogicalRuleType) logicalRuleDefinition.getRuleType();

        Rule leftRule = create(logicalRuleDefinition.getLeft());
        Rule rightRule = create(logicalRuleDefinition.getRight());

        return switch (type) {
            case AND -> new AndRule(leftRule, rightRule);
            case OR -> new OrRule(leftRule, rightRule);
            case XOR -> new XorRule(leftRule, rightRule);
            case NOT -> new NotRule(leftRule);
        };
    }

    public Rule createLeafRule(LeafRuleDefinition leafRuleDefinition) {
        LeafRuleType leafRuleType = (LeafRuleType) leafRuleDefinition.getRuleType();
        List<Indicator<Num>> indicators = leafRuleDefinition.getIndicatorDefinitions().stream()
            .map(indicatorFactory::create).toList();

        return switch (leafRuleType) {
            case OVER_INDICATOR -> buildOverIndicatorRule(leafRuleDefinition, indicators);
            case UNDER_INDICATOR -> buildUnderIndicatorRule(leafRuleDefinition, indicators);
            case CROSSED_UP_INDICATOR -> buildCrossedUpIndicatorRule(leafRuleDefinition, indicators);
            case CROSSED_DOWN_INDICATOR -> buildCrossedDownIndicatorRule(leafRuleDefinition, indicators);
            case IS_RISING -> buildIsRisingRule(leafRuleDefinition, indicators);
            case IS_FALLING -> buildIsFallingRule(leafRuleDefinition, indicators);
            case IS_EQUAL -> buildIsEqualRule(leafRuleDefinition, indicators);
            case WITHIN_PERCENTAGE -> buildWithinPercentageRule(leafRuleDefinition, indicators);
            case STOP_LOSS -> buildStopLossRule(leafRuleDefinition, indicators);
            case STOP_GAIN -> buildStopGainRule(leafRuleDefinition, indicators);
            case TRAILING_STOP_LOSS -> buildTrailingStopLossRule(leafRuleDefinition, indicators);
            case AVERAGE_TRUE_RANGE_STOP_GAIN ->
                buildAverageTrueRangeStopGainRule(leafRuleDefinition, indicators);
            case AVERAGE_TRUE_RANGE_STOP_LOSS ->
                buildAverageTrueRangeStopLossRule(leafRuleDefinition, indicators);
            case AVERAGE_TRUE_RANGE_TRAILING_STOP_LOSS ->
                buildAverageTrueRangeTrailingStopLossRule(leafRuleDefinition, indicators);
        };
    }

    private Rule buildOverIndicatorRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS.getSignature())) {
                    return new OverIndicatorRule(indicators.get(0), indicators.get(1));
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new OverIndicatorRule(indicators.get(0), threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for OVER_INDICATOR");
    }

    private Rule buildUnderIndicatorRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS.getSignature())) {
                    return new UnderIndicatorRule(indicators.get(0), indicators.get(1));
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new UnderIndicatorRule(indicators.get(0), threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for UNDER_INDICATOR");
    }

    private Rule buildCrossedUpIndicatorRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS.getSignature())) {
                    return new CrossedUpIndicatorRule(indicators.get(0), indicators.get(1));
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new CrossedUpIndicatorRule(indicators.get(0), threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for CROSSED_UP_INDICATOR");
    }

    private Rule buildCrossedDownIndicatorRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS.getSignature())) {
                    return new CrossedDownIndicatorRule(indicators.get(0), indicators.get(1));
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new CrossedDownIndicatorRule(indicators.get(0), threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for CROSSED_DOWN_INDICATOR");
    }

    private Rule buildIsEqualRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS.getSignature())) {
                    return new IsEqualRule(indicators.get(0), indicators.get(1));
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new IsEqualRule(indicators.get(0), threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for IS_EQUAL");
    }

    private Rule buildIsRisingRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD.getSignature())) {
                    int barCount = (int) ruleDefinition.getParameters().get(RuleParam.BAR_COUNT);
                    double threshold = (double) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new IsRisingRule(indicators.get(0), barCount, threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for IS_RISING");
    }

    private Rule buildIsFallingRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD.getSignature())) {
                    int barCount = (int) ruleDefinition.getParameters().get(RuleParam.BAR_COUNT);
                    double threshold = (double) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    return new IsFallingRule(indicators.get(0), barCount, threshold);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for IS_FALLING");
    }

    private Rule buildWithinPercentageRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.TWO_INDICATORS_WITH_PERCENTAGE.getSignature())) {
                    double percentage = (double) ruleDefinition.getParameters().get(RuleParam.PERCENTAGE);
                    return new WithinPercentageRule(indicators.get(0), indicators.get(1), percentage);
                } else if (signature.equals(LeafRuleSignature.Types.ONE_INDICATOR_WITH_THRESHOLD_AND_PERCENTAGE.getSignature())) {
                    Number threshold = (Number) ruleDefinition.getParameters().get(RuleParam.THRESHOLD);
                    double percentage = (double) ruleDefinition.getParameters().get(RuleParam.PERCENTAGE);
                    return new WithinPercentageRule(indicators.get(0), threshold, percentage);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for WITHIN_PERCENTAGE");
    }

    private Rule buildStopLossRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.CLOSE_PRICE_WITH_PERCENTAGE.getSignature())) {
                    Number percentage = (Number) ruleDefinition.getParameters().get(RuleParam.PERCENTAGE);
                    return new StopLossRule((ClosePriceIndicator) indicators.get(0), percentage);
                }

            }
        }
        throw new IllegalArgumentException("Invalid rule signature for STOP_LOSS");
    }

    private Rule buildStopGainRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.CLOSE_PRICE_WITH_PERCENTAGE.getSignature())) {
                    Number percentage = (Number) ruleDefinition.getParameters().get(RuleParam.PERCENTAGE);
                    return new StopGainRule((ClosePriceIndicator) indicators.get(0), percentage);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for STOP_GAIN");
    }

    private Rule buildTrailingStopLossRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                if (signature.equals(LeafRuleSignature.Types.PRICE_WITH_PERCENTAGE_AND_BAR_COUNT.getSignature())) {
                    double percentage = (double) ruleDefinition.getParameters().get(RuleParam.PERCENTAGE);
                    int barCount = (int) ruleDefinition.getParameters().get(RuleParam.BAR_COUNT);
                    return new TrailingStopLossRule(indicators.get(0), barSeries.numOf(percentage), barCount);
                }
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for TRAILING_STOP_LOSS");
    }

    private Rule buildAverageTrueRangeStopGainRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                double coefficient = (double) ruleDefinition.getParameters().get(RuleParam.COEFFICIENT);
                int atrBarCount = (int) ruleDefinition.getParameters().get(RuleParam.ATR_BAR_COUNT);
                return new AverageTrueRangeStopGainRule(barSeries, indicators.get(0), atrBarCount, coefficient);
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for AVERAGE_TRUE_RANGE_STOP_GAIN");
    }

    private Rule buildAverageTrueRangeStopLossRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                double coefficient = (double) ruleDefinition.getParameters().get(RuleParam.COEFFICIENT);
                int atrBarCount = (int) ruleDefinition.getParameters().get(RuleParam.ATR_BAR_COUNT);
                return new AverageTrueRangeStopLossRule(barSeries, indicators.get(0), atrBarCount, coefficient);
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for AVERAGE_TRUE_RANGE_STOP_LOSS");
    }

    private Rule buildAverageTrueRangeTrailingStopLossRule(LeafRuleDefinition ruleDefinition, List<Indicator<Num>> indicators) {
        for (RuleSignature signature : ruleDefinition.getRuleType().allowedSignatures()) {
            if (signature.matches(ruleDefinition)) {
                double coefficient = (double) ruleDefinition.getParameters().get(RuleParam.COEFFICIENT);
                int atrBarCount = (int) ruleDefinition.getParameters().get(RuleParam.ATR_BAR_COUNT);
                return new AverageTrueRangeTrailingStopLossRule(barSeries, indicators.get(0), atrBarCount, coefficient);
            }
        }
        throw new IllegalArgumentException("Invalid rule signature for AVERAGE_TRUE_RANGE_TRAILING_STOP_LOSS");
    }
}
