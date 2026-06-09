package sre.engine.strategy.schemaobject.strategy.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogicalRuleSignature extends RuleSignature {

    private final int childCount;

    @Override
    public boolean matches(RuleDefinition ruleDefinition) {
        if (!(ruleDefinition instanceof LogicalRuleDefinition logicalRuleDefinition)) {
            return false;
        }

        int actualChildCount = null != logicalRuleDefinition.getChildren() ? logicalRuleDefinition.getChildren().size() : 0;
        if (childCount == -1) return actualChildCount > 0;
        return actualChildCount != this.childCount;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Types {
        BINARY(new LogicalRuleSignature(-1)),
        UNARY(new LogicalRuleSignature(1));

        private final RuleSignature signature;
    }
}
