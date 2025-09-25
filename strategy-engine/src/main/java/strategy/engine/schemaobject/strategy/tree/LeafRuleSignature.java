package strategy.engine.schemaobject.strategy.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import strategy.engine.constant.RuleParam;
import strategy.engine.constant.enums.IndicatorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class LeafRuleSignature extends RuleSignature {

    private final int expectedIndicatorCount;
    private final List<Set<IndicatorType>> allowedIndicatorTypes;
    private final Set<String> requiredParameters;


    @Override
    public boolean matches(RuleDefinition ruleDefinition) {
        if (!(ruleDefinition instanceof LeafRuleDefinition leafRuleDefinition)) {
            return false;
        }

        List<IndicatorDefinition> actualIndicatorDefinitions =
            null == leafRuleDefinition.getIndicatorDefinitions() ?
                new ArrayList<>() :
                leafRuleDefinition.getIndicatorDefinitions();

        Map<String, Object> actualParameters =
            null == leafRuleDefinition.getParameters() ?
                new HashMap<>() :
                leafRuleDefinition.getParameters();

        if (actualIndicatorDefinitions.size() != expectedIndicatorCount) {
            return false;
        }

        for (int i = 0; i < expectedIndicatorCount; i++) {
            Set<IndicatorType> allowedTypes = allowedIndicatorTypes.get(i);
            IndicatorType actualType = actualIndicatorDefinitions.get(i).getType();
            if (!allowedTypes.contains(actualType)) {
                return false;
            }
        }

        return actualParameters.keySet().containsAll(requiredParameters);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Types {
        TWO_INDICATORS(new LeafRuleSignature(
            2,
            List.of(Set.of(IndicatorType.values()), Set.of(IndicatorType.values())),
            null)),
        ONE_INDICATOR_WITH_THRESHOLD(new LeafRuleSignature(
            1,
            List.of(Set.of(IndicatorType.values())),
            Set.of(RuleParam.THRESHOLD)
        )),
        ONE_INDICATOR_WITH_BAR_COUNT_AND_THRESHOLD(
            new LeafRuleSignature(
                1,
                List.of(Set.of(IndicatorType.values())),
                Set.of(RuleParam.BAR_COUNT, RuleParam.THRESHOLD)
            )
        ),
        TWO_INDICATORS_WITH_PERCENTAGE(
            new LeafRuleSignature(
                2,
                List.of(Set.of(IndicatorType.values())),
                Set.of(RuleParam.PERCENTAGE)
            )
        ),
        ONE_INDICATOR_WITH_THRESHOLD_AND_PERCENTAGE(
            new LeafRuleSignature(
                1,
                List.of(
                    Set.of(IndicatorType.values())
                ),
                Set.of(RuleParam.THRESHOLD, RuleParam.PERCENTAGE)
            )
        ),
        CLOSE_PRICE_WITH_PERCENTAGE(
            new LeafRuleSignature(
                1,
                List.of(Set.of(IndicatorType.CLOSE_PRICE)),
                Set.of(RuleParam.PERCENTAGE))
        ),
        PRICE_WITH_PERCENTAGE(
            new LeafRuleSignature(
                1,
                List.of(Set.of(IndicatorType.CLOSE_PRICE, IndicatorType.HIGH_PRICE)),
                Set.of(RuleParam.PERCENTAGE))
        ),
        PRICE_WITH_PERCENTAGE_AND_BAR_COUNT(
            new LeafRuleSignature(
                1,
                List.of(Set.of(IndicatorType.CLOSE_PRICE, IndicatorType.HIGH_PRICE)),
                Set.of(RuleParam.PERCENTAGE, RuleParam.BAR_COUNT))
        ),
        PRICE_WITH_ATR_BAR_COUNT_AND_COEFFICIENT(
            new LeafRuleSignature(
                1,
                List.of(Set.of(IndicatorType.CLOSE_PRICE, IndicatorType.HIGH_PRICE)),
                Set.of(RuleParam.ATR_BAR_COUNT, RuleParam.COEFFICIENT))
        );



        private final RuleSignature signature;
    }
}
