package strategy.engine.schemaobject.strategy.tree;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import strategy.engine.constant.enums.LeafRuleType;

import java.util.List;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LeafRuleDefinition extends RuleDefinition {

    private final List<IndicatorDefinition> indicatorDefinitions;
    private final Map<String, Object> parameters;

    public LeafRuleDefinition(LeafRuleType ruleType, List<IndicatorDefinition> indicatorDefinitions, Map<String, Object> parameters) {
        super(ruleType);
        this.indicatorDefinitions = indicatorDefinitions;
        this.parameters = parameters;
    }
}
