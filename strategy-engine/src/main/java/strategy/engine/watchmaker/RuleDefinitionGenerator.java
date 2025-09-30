package strategy.engine.watchmaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.constant.RuleParam;
import strategy.engine.constant.enums.IndicatorType;
import strategy.engine.constant.enums.LeafRuleType;
import strategy.engine.constant.enums.LogicalRuleType;
import strategy.engine.constant.enums.RuleType;
import strategy.engine.schemaobject.strategy.tree.IndicatorDefinition;
import strategy.engine.schemaobject.strategy.tree.LeafRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LeafRuleSignature;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleDefinition;
import strategy.engine.schemaobject.strategy.tree.LogicalRuleSignature;
import strategy.engine.schemaobject.strategy.tree.RuleDefinition;
import strategy.engine.schemaobject.strategy.tree.RuleSignature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class RuleDefinitionGenerator {

    private final IndicatorDefinitionGenerator indicatorDefinitionGenerator;

    public RuleDefinition generate(Random rng) {
        if (rng.nextDouble() < 0.5) {
            return generate(LogicalRuleDefinition.class, rng);
        }

        return generate(LeafRuleDefinition.class, rng);
    }

    public RuleDefinition generate(Class<? extends RuleDefinition> clazz, Random rng) {
        RuleType[] ruleTypes;
        if (LogicalRuleDefinition.class.equals(clazz)) {
            ruleTypes = LogicalRuleType.values();
        } else if (LeafRuleDefinition.class.equals(clazz)) {
            ruleTypes = LeafRuleType.values();
        } else {
            throw new IllegalArgumentException("Input must be an inheritor of RuleDefinition");
        }

        return generate(ruleTypes[rng.nextInt(ruleTypes.length)], rng);
    }

    public RuleDefinition generate(RuleType ruleType, Random rng) {
        List<RuleSignature> signatures = ruleType.allowedSignatures();
        RuleSignature signature = signatures.get(rng.nextInt(signatures.size()));
        if (ruleType instanceof LogicalRuleType logicalRuleType) {
            return generate(logicalRuleType, (LogicalRuleSignature) signature, rng);
        }
        return generate((LeafRuleType) ruleType, (LeafRuleSignature) signature, rng);
    }

    private RuleDefinition generate(LogicalRuleType ruleType, LogicalRuleSignature signature, Random rng) {
        return new LogicalRuleDefinition(ruleType, List.of(generate(LeafRuleDefinition.class, rng)));
    }

    private RuleDefinition generate(LeafRuleType ruleType, LeafRuleSignature leafRuleSignature, Random rng) {
        List<IndicatorDefinition> indicatorDefinitions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        int indicatorCount = leafRuleSignature.getExpectedIndicatorCount();
        List<Set<IndicatorType>> allowedIndicators = leafRuleSignature.getAllowedIndicatorTypes();
        for (int i=0;i<indicatorCount;i++) {
            List<IndicatorType> types = new ArrayList<>(allowedIndicators.get(i));
            indicatorDefinitions.add(indicatorDefinitionGenerator.generate(types.get(rng.nextInt(types.size())), rng));
        }

        leafRuleSignature.getRequiredParameters().forEach( key -> {
                Object[] possibleValues = switch (key) {
                    case RuleParam.BAR_COUNT -> getBarCount(ruleType);
                    case RuleParam.THRESHOLD -> getThreshold(ruleType, leafRuleSignature, indicatorDefinitions);
                    case RuleParam.PERCENTAGE -> getPercentage(ruleType);
                    case RuleParam.ATR_BAR_COUNT -> getAtrBarCount(ruleType);
                    case RuleParam.COEFFICIENT -> getCoefficient(ruleType);
                    default -> null;
                };
                parameters.put(key, null == possibleValues ? null : possibleValues[rng.nextInt(possibleValues.length)]);
            }
        );

        return new LeafRuleDefinition(ruleType, indicatorDefinitions, parameters);
    }

    private Number[] getBarCount(LeafRuleType ruleType) {
        return new Number[]{1, 2, 5, 10}; // same values for all rule types
    }

    private Number[] getThreshold(LeafRuleType ruleType, LeafRuleSignature leafRuleSignature, List<IndicatorDefinition> indicatorDefinitions) {

        List<IndicatorType> boundedIndicators = List.of(IndicatorType.ATR, IndicatorType.RSI);
        if (LeafRuleSignature.Types.ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD.getSignature().equals(leafRuleSignature)) {
            return new Number[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        }

        if (boundedIndicators.contains(indicatorDefinitions.get(0).getIndicatorType())) {
            return new Number[]{10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90};
        }
        // for unbounded types - CLOSE_PRICE, HIGH_PRICE, LOW_PRICE, RECENT_SWING_HIGH
        Number[] possibleValues = new Number[2000];
        for (int i = 1;i <= 2000; i++) {
            possibleValues[i-1] = i;
        }
        return possibleValues;
    }

    private Number[] getPercentage(LeafRuleType ruleType) {
        return switch (ruleType) {
            case STOP_GAIN -> new Number[]{2, 5, 10, 15};
            default -> new Number[]{1, 2, 5};
        };
    }

    private Number[] getAtrBarCount(LeafRuleType ruleType) {
        return new Number[]{4,6,8,10,12,14,16,18,20};
    }

    private Number[] getCoefficient(LeafRuleType ruleType) {
        return new Number[]{0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};
    }
}
