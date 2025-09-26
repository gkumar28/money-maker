package strategy.engine.schemaobject.strategy.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import strategy.engine.constant.enums.LogicalRuleType;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalRuleDefinition extends RuleDefinition {

    private final RuleDefinition left;
    private final RuleDefinition right;

    @JsonCreator
    public LogicalRuleDefinition(@JsonProperty("rule_type") LogicalRuleType ruleType,
                                 @JsonProperty("left") RuleDefinition left,
                                 @JsonProperty("right") RuleDefinition right) {
        super(ruleType);
        this.left = left;
        this.right = right;
    }
}
