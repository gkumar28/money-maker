package strategy.engine.schemaobject.strategy.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public LeafRuleDefinition(
        @JsonProperty("rule_type") LeafRuleType ruleType,
        @JsonProperty("indicator_definitions") List<IndicatorDefinition> indicatorDefinitions,
        @JsonProperty("parameters") Map<String, Object> parameters) {
        super(ruleType);
        this.indicatorDefinitions = indicatorDefinitions;
        this.parameters = parameters;
    }
}
