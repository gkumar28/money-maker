package strategy.engine.schemaobject.strategy.tree;

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

    public LogicalRuleDefinition(LogicalRuleType ruleType, RuleDefinition left, RuleDefinition right) {
        super(ruleType);
        this.left = left;
        this.right = right;
    }
}
