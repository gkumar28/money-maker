package sre.engine.strategy.schemaobject.strategy.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sre.engine.strategy.constant.enums.LogicalRuleType;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalRuleDefinition extends RuleDefinition {

    private final List<RuleDefinition> children;

    @JsonCreator
    public LogicalRuleDefinition(@JsonProperty("rule_type") LogicalRuleType ruleType,
                                 @JsonProperty("children") List<RuleDefinition> children) {
        super(ruleType);
        this.children = children;
    }

    public LogicalRuleDefinition withChildren(List<RuleDefinition> otherChildren) {
        if (this.children != null && this.children.equals(otherChildren)) {
            return this;
        }

        return new LogicalRuleDefinition((LogicalRuleType) this.ruleType, otherChildren);
    }
}
